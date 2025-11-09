package yunhan.supplement.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "emotionapi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Emotionapi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int emotionapiId;

    @Column(name = "diary_id", nullable = false)  // ✅ DB 컬럼 이름 명확하게 지정
    private int diaryId;

    @Column(name = "emotion")
    private String emotion;
}
