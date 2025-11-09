package yunhan.supplement.DTO;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DiaryDTO {  // ✅ DiaryDto → DiaryDTO (일관된 이름 사용)
    private int diaryId;
    private String title;
    //
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;
}

//
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//
//import java.time.LocalDate;
//
//@Getter
//@AllArgsConstructor
//public class DiaryDTO {
//    private String title;
//    private LocalDate date;
//}


//위에거랑 같은거임
//public class DiaryDTO {
//    private String title;
//    private LocalDate date;
//
//    public DiaryDTO(String title, LocalDate date) {
//        this.title = title;
//        this.date = date;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public LocalDate getDate() {
//        return date;
//    }
//}

