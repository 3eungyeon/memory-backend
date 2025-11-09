package yunhan.supplement.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeCapsuleDTO {
    private int id;
    private String title;
    private String content;
    private String imagePath;
    private LocalDateTime openDate;
    private Boolean isOpened;


}
