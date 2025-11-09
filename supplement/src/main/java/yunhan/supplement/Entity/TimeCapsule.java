package yunhan.supplement.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter @Getter
@Table(name = "timecapsule")
public class TimeCapsule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int timecapsuleId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private String imagePath;

    @Column(nullable = false)
    private LocalDateTime openDate;

    @Column(nullable = false)
    private Boolean isOpened = false;

    @ManyToMany
    @JoinTable(
            name = "timecapsule_users",
            joinColumns = @JoinColumn(name = "timecapsule_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<UserEntity> users = new ArrayList<>();

    // Getters & Setters 생략
}
