package konkuk.aiku.service;

import konkuk.aiku.domain.*;
import konkuk.aiku.event.UserPointEventPublisher;
import konkuk.aiku.exception.ErrorCode;
import konkuk.aiku.exception.NoAthorityToAccessException;
import konkuk.aiku.exception.NoSuchEntityException;
import konkuk.aiku.repository.BettingRepository;
import konkuk.aiku.repository.ScheduleRepository;
import konkuk.aiku.repository.UsersRepository;
import konkuk.aiku.service.dto.BettingServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class BettingService {

    private final BettingRepository bettingRepository;
    private final ScheduleRepository scheduleRepository;
    private final UsersRepository usersRepository;
    private final UserPointEventPublisher userPointEventPublisher;


    private Optional<UserSchedule> findUserInSchedule(Long userId, Long scheduleId) {
        return scheduleRepository.findUserScheduleByUserIdAndScheduleId(userId, scheduleId);
    }

    private Users findUserById(Long userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() -> new NoSuchEntityException(ErrorCode.NO_SUCH_USER));
    }

    private Betting findBettingById(Long bettingId) {
        return bettingRepository.findById(bettingId)
                .orElseThrow(() -> new NoSuchEntityException(ErrorCode.NO_SUCH_BETTING));
    }

    public BettingServiceDto findBetting(Long bettingId) {
        Betting betting = findBettingById(bettingId);

        return BettingServiceDto.toServiceDto(betting);
    }

    public Long addBetting(Users users, Long scheduleId, BettingServiceDto bettingServiceDto) {
        Optional<UserSchedule> userInSchedule = findUserInSchedule(users.getId(), scheduleId);

        if (userInSchedule.isEmpty()) {
            throw new NoAthorityToAccessException(ErrorCode.NO_ATHORITY_TO_ACCESS);
        }
        Users targetUser = usersRepository.findById(bettingServiceDto.getTargetUser().getUserId())
                .orElseThrow(() -> new NoSuchEntityException(ErrorCode.NO_SUCH_USER));

        UserSchedule userSchedule = userInSchedule.get();
        Schedule schedule = userSchedule.getSchedule();

        Betting betting = Betting.builder()
                .bettor(users)
                .targetUser(targetUser)
                .point(bettingServiceDto.getPoint())
                .schedule(schedule)
                .bettingType(bettingServiceDto.getBettingType())
                .bettingStatus(BettingStatus.WAIT)
                .build();

        bettingRepository.save(betting);

        // 1대1 레이싱인 경우
        if (betting.getBettingType().equals(BettingType.RACING)) {
            // TODO : 스케줄러 로직 (1분)
            // TODO : 베팅 상대에게 알림 메시지
        } else {
            // 베팅인 경우
            // 베팅 금액 지불
            users.minusPoint(betting.getPoint());
            betting.setBettingStatus(BettingStatus.ACCEPT);
            schedule.addBetting(betting);
        }

        return betting.getId();
    }

    public Long acceptBetting(Long bettingId) {
        Betting betting = findBettingById(bettingId);
        betting.setBettingStatus(BettingStatus.ACCEPT);

        // TODO : 베팅 주인에게 수락 알림 메시지

        Users bettor = betting.getBettor();
        Users targetUser = betting.getTargetUser();

        // 베팅 거는 비용
        int point = betting.getPoint();

        bettor.minusPoint(point);
        targetUser.minusPoint(point);

        // schedule에 레이싱 추가
        betting.getSchedule().addRacing(betting);

        return betting.getId();
    }

    // TODO : 레이싱 미수락 시 베팅 삭제 로직
    public Long deleteBettingById(Long bettingId) {
        Betting betting = findBettingById(bettingId);
        // 베팅이 수락되지 않은 경우
        if (!betting.getBettingStatus().equals(BettingStatus.ACCEPT)) {
            bettingRepository.deleteById(bettingId);
        }

        return bettingId;
    }


    // 베팅 업데이트 로직 미사용(Deprecated)
    public Long updateBetting(Users users, Long scheduleId, Long bettingId, BettingServiceDto bettingServiceDto) {
        Optional<UserSchedule> userInSchedule = findUserInSchedule(users.getId(), scheduleId);

        if (userInSchedule.isEmpty()) {
            throw new NoAthorityToAccessException(ErrorCode.NO_ATHORITY_TO_ACCESS);
        }
        // Schedule 검증
        Betting betting = findBettingById(bettingId);
        Users target = findUserById(bettingServiceDto.getTargetUser().getUserId());

        betting.setTargetUser(target);
        betting.setPoint(bettingServiceDto.getPoint());

        return betting.getId();
    }

    public Long deleteBetting(Users users, Long scheduleId, Long bettingId) {
        Optional<UserSchedule> userInSchedule = findUserInSchedule(users.getId(), scheduleId);

        if (userInSchedule.isEmpty()) {
            throw new NoAthorityToAccessException(ErrorCode.NO_ATHORITY_TO_ACCESS);
        }
        // Schedule 검증
        bettingRepository.deleteById(bettingId);

        return bettingId;
    }

    public List<BettingServiceDto> getBettingsByType(Long scheduleId, String bettingType) {
        List<Betting> bettings = bettingRepository.findBettingsByScheduleIdAndBettingType(scheduleId, BettingType.valueOf(bettingType));

        return bettings.stream()
                .map(BettingServiceDto::toServiceDto)
                .collect(Collectors.toList());
    }

    /**
     * 이벤트 발생 메서드
     * 스케줄 종료시 레이싱 결과 생성 로직
     * @param scheduleId 도착한 유저가 속한 스케줄
     * @return 유저 아이디
     * TODO: 유저 도착 시 해당 메소드 실행
     */
    public Long userRacingArrival(Long scheduleId, Long userId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(() -> new NoSuchEntityException(ErrorCode.NO_SUCH_SCHEDULE));

        List<Betting> racings = schedule.getRacings();

        for (Betting racing : racings) {
            // 도착 유저에 대해서 아직 종료되지 않은 레이싱만 실행
            if (racing.getBettor().getId() == userId && racing.getBettingStatus().equals(BettingStatus.ACCEPT)) {
                setRacingResult(racing);
            }
        }

        return userId;
    }
    public void setRacingResult(Betting betting) {
        Users bettor = betting.getBettor();
        Users targetUser = betting.getTargetUser();

        ResultType resultType;
        int point = betting.getPoint();

        // 플러스 포인트 : 베팅 걸린 시점에서 베팅 포인트 가져갔기 때문에 2배로 더해준다.
        int plusPoint = point * 2;

        // 베팅 건 사람 플러스 포인트
        bettor.plusPoint(plusPoint);

        userPointEventPublisher.userPointChangeEvent(bettor, plusPoint, PointType.BETTING, PointChangeType.PLUS, LocalDateTime.now());

        betting.updateBettingResult(ResultType.WIN);
        betting.setBettingStatus(BettingStatus.DONE);
    }

    /**
     * 이벤트 발생 메서드
     * 스케줄 종료시 베팅 & 레이싱 결과 생성 로직
     * @return 해당 스케줄 아이디
     * TODO: Schedule 종료 시 해당 메소드 실행
     */
    public Long setAllBettings(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(() -> new NoSuchEntityException(ErrorCode.NO_SUCH_SCHEDULE));

        List<Betting> bettings = schedule.getBettings();
        // 베팅 결과 설정
        for (Betting betting : bettings) {
            setBettingResult(betting);
        }
        // 베팅 전체 결과에 따른 포인트 분배
        bettingPointCalculate(bettings);

        // 둘 다 지각한 경우에 대한 레이싱 로직
        List<Betting> racings = schedule.getRacings();
        for (Betting racing : racings) {
            // 아직 레이싱이 종료되지 않은 레이싱에 대해 실행
            if (racing.getBettingStatus().equals(BettingStatus.ACCEPT)) {
                setDrawRacing(racing);
            }
        }
        return scheduleId;
    }

    public void setBettingResult(Betting betting) {
        Users targetUser = betting.getTargetUser();

        Schedule schedule = betting.getSchedule();
        // 유저 도착 정보 정렬
        List<UserArrivalData> userArrivalDatas = schedule.getUserArrivalDatas();
        userArrivalDatas.sort(Comparator.comparing(UserArrivalData::getArrivalTime));
        UserArrivalData lastArrivalData = userArrivalDatas.get(userArrivalDatas.size() - 1);

        ResultType resultType;

        if (lastArrivalData.getUser().getId() == targetUser.getId()) {
            // 베팅 성공!
            resultType = ResultType.WIN;
        } else {
            // 베팅 실패..
            resultType = ResultType.LOSE;
        }

        betting.updateBettingResult(resultType);
        betting.setBettingStatus(BettingStatus.DONE);

    }

    public void bettingPointCalculate(List<Betting> bettings) {
        int bettingPoints = 0;
        int winners = 0;
        for (Betting betting : bettings) {
            bettingPoints += betting.getPoint();
            if (betting.getResultType().equals(ResultType.WIN)) {
                winners += 1;
            }
        }
        int reward = bettingPoints / winners;

        for (Betting betting : bettings) {
            if (betting.getResultType().equals(ResultType.WIN)) {
                betting.getBettor().plusPoint(reward);
            }
        }

    }

    public void setDrawRacing(Betting betting) {
        int point = betting.getPoint();
        betting.setResultType(ResultType.DRAW);
        // 베팅 건 금액 돌려주기
        Users bettor = betting.getBettor();
        Users targetUser = betting.getTargetUser();

        bettor.plusPoint(point);
        targetUser.plusPoint(point);

        userPointEventPublisher.userPointChangeEvent(bettor, point, PointType.BETTING, PointChangeType.PLUS, LocalDateTime.now());
        userPointEventPublisher.userPointChangeEvent(targetUser, point, PointType.BETTING, PointChangeType.PLUS, LocalDateTime.now());

    }
}
