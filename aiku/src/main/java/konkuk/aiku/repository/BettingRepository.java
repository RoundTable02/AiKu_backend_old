package konkuk.aiku.repository;

import konkuk.aiku.domain.Betting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BettingRepository extends JpaRepository<Betting, Long> {
}
