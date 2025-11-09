package yunhan.supplement.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "friendship")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long friendshipId;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private UserEntity sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserEntity receiver;

    @Column(nullable = false)
    private boolean isFriend; // 요청 상태 (false: 요청 중, true: 친구)

    // 명시적인 생성자 추가 (Lombok이 안될 경우)
    public Friendship(UserEntity sender, UserEntity receiver, boolean isFriend) {
        this.sender = sender;
        this.receiver = receiver;
        this.isFriend = isFriend;
    }
}
