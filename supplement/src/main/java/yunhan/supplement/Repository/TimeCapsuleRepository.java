package yunhan.supplement.Repository;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.repository.JpaRepository;
import yunhan.supplement.Entity.TimeCapsule;
import yunhan.supplement.Entity.UserEntity;
import java.util.List;


public interface TimeCapsuleRepository extends JpaRepository<TimeCapsule, Integer> {
    List<TimeCapsule> findByUsersContains(UserEntity user);
}
