package yunhan.supplement.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "diary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int diaryId;

    @Column(nullable = false, length = 255)
    private String title;

    @JsonProperty("user_id")  // ✅ JSON 요청에서 "user_id"와 매핑
    @Column(name = "user_id", nullable = false)  // ✅ DB 컬럼 이름 명확하게 지정
    private int userId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "imageUrl")
    private String imageUrl;

    @Column(length = 50)
    private String weather;


    @Column(nullable = false)
    private LocalDate date;


}

