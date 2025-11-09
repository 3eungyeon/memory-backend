package yunhan.supplement.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import yunhan.supplement.Entity.Diary;
import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Integer> {
    List<Diary> findByUserId(int userId); // ✅ userId 기준으로 다이어리 조회
}

//package yunhan.supplement.Repository;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import yunhan.supplement.Entity.Diary;
//
//public interface DiaryRepository extends JpaRepository<Diary, Integer> {
//}
