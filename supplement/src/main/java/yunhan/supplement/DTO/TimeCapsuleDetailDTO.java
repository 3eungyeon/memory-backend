package yunhan.supplement.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeCapsuleDetailDTO {
    private Long timecapsuleId;
    private String title;
    private String content;
    private String imagePath;
    private LocalDateTime openDate;
    private Boolean isOpened;
    //private List<Integer> userIds;  // ✅ 공유된 친구 ID 리스트
    private List<String> usernames; // ✅ 공유된 친구 이름 리스트

}
