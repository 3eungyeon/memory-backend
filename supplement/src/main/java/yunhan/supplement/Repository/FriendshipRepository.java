package yunhan.supplement.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yunhan.supplement.Entity.Friendship;
import yunhan.supplement.Entity.UserEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    Optional<Friendship> findBySenderAndReceiver(UserEntity sender, UserEntity receiver);
    List<Friendship> findBySenderAndIsFriendFalse(UserEntity sender); // ✅ 실제로 팔로우 받은 친구 요청 조회
    List<Friendship> findBySenderAndIsFriendTrue(UserEntity sender);
    List<Friendship> findByReceiverAndIsFriendTrue(UserEntity receiver);
    List<Friendship> findByReceiverAndIsFriendFalse(UserEntity receiver); // ✅ 받은 친구 요청 조회
    // ✅ 친구 관계 찾기 (양방향)
    //Optional<Friendship> findBySenderAndReceiver(UserEntity sender, UserEntity receiver);
    Optional<Friendship> findByReceiverAndSender(UserEntity receiver, UserEntity sender);
    // ✅ 친구 관계 삭제 (한쪽만 남는 문제 방지)
    @Transactional
    @Modifying
    @Query("DELETE FROM Friendship f WHERE (f.sender = :user1 AND f.receiver = :user2) OR (f.sender = :user2 AND f.receiver = :user1)")
    void deleteFriendship(@Param("user1") UserEntity user1, @Param("user2") UserEntity user2);


    @Query("SELECT COUNT(f) FROM Friendship f " +
            "JOIN Friendship sf ON f.receiver.id = sf.sender.id AND f.sender.id = sf.receiver.id " +
            "WHERE (f.sender.id = :userId) " +
            " AND f.isFriend = true" +
            " AND sf.isFriend = TRUE")
    int countFriends(@Param("userId") int userId);
}

