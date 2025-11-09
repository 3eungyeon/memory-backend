package yunhan.supplement.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yunhan.supplement.Entity.Emotionapi;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmotionapiRepository extends JpaRepository<Emotionapi, Integer> {
    Optional<Emotionapi> findByDiaryId(int diaryId);
}
