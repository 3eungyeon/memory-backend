package yunhan.supplement.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yunhan.supplement.Entity.UserEntity;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    boolean existsByUsername(String username);
    Optional<UserEntity> findByUsername(String username);  // 이 부분 추가

    @Query("SELECT u.name FROM UserEntity u WHERE u.id = :userId")
    String findUserNameById(@Param("userId") int userId);




}







//package yunhan.supplement.Repository;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//import yunhan.supplement.Entity.UserEntity;
//
//
//
//@Repository
//public interface UserRepository extends JpaRepository<UserEntity, Long> {
//    boolean existsByUsername(String username);  // 사용자 이름이 존재하는지 체크
//}


