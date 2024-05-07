package konkuk.aiku.service;

import konkuk.aiku.controller.dto.SettingAlarmDTO;
import konkuk.aiku.controller.dto.SettingAuthorityDTO;
import konkuk.aiku.controller.dto.UserUpdateDTO;
import konkuk.aiku.domain.Setting;
import konkuk.aiku.domain.Users;
import konkuk.aiku.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Users save(Users users) {
        users.setPassword(passwordEncoder.encode(users.getKakaoId()));
        return usersRepository.save(users);
    }

    @Transactional
    public Users findById(Long id) {
        return usersRepository.findById(id).orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
    }

    @Transactional
    public Users findByKakaoId(String kakaoId) {
        return usersRepository.findByKakaoId(kakaoId).orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));
    }

    @Transactional
    public void logout(String kakaoId) {
        Users user = findByKakaoId(kakaoId);
        user.setRefreshToken(null);
    }

    @Transactional
    public void updateUser(Users users, UserUpdateDTO userUpdateDTO) {
        users.updateUser(userUpdateDTO);
    }

    @Transactional
    public void deleteUsers(Users users) {
        usersRepository.delete(users);
    }

    @Transactional
    public void setAlarm(Users users, SettingAlarmDTO settingAlarmDTO) {
        Setting setting = settingAlarmDTO.toSetting(users.getSetting());
        users.updateSetting(setting);
    }

    public void setAuthority(Users users, SettingAuthorityDTO settingAuthorityDTO) {
        Setting setting = settingAuthorityDTO.toSetting(users.getSetting());
        users.updateSetting(setting);
    }

    public Setting getAlarm(Users users) {
        return users.getSetting();
    }

    public Setting getAuthority(Users users) {
        return users.getSetting();
    }
}
