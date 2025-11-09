
package yunhan.supplement.Service;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import yunhan.supplement.Entity.Diary;
import yunhan.supplement.Entity.Emotionapi;
import yunhan.supplement.Repository.DiaryRepository;
import yunhan.supplement.Repository.EmotionapiRepository;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public class DiaryService {
    @Autowired private DiaryRepository diaryRepository;
    @Autowired private EmotionapiRepository emotionapiRepository;

    // 동기 + 캐시: key는 단순히 userId
    @Cacheable(cacheNames = "diariesByUser", key = "#userId")
    public List<Diary> findDiariesByUserId(int userId) {
        return diaryRepository.findByUserId(userId);
    }

    @Async("appExecutor")
    public CompletableFuture<List<Diary>> getDiariesByUserIdAsync(int userId) {
        return CompletableFuture.supplyAsync(() -> findDiariesByUserId(userId));
    }

    public Optional<Diary> getDiaryById(int diaryId) {
        return diaryRepository.findById(diaryId);
    }

    @Async("appExecutor")
    @Transactional
    // 생성 시 해당 유저 목록 캐시 무효화
    @CacheEvict(cacheNames = "diariesByUser", key = "#userId")
    public CompletableFuture<Void> saveDiaryAsync(int userId, String title, String content, String imageUrl, String weather, String date) {
        return CompletableFuture.runAsync(() -> {
            Diary diary = new Diary();
            diary.setUserId(userId);
            diary.setTitle(title);
            diary.setContent(content);
            diary.setImageUrl(imageUrl);
            diary.setWeather(weather);
            diary.setDate(LocalDate.parse(date));
            diaryRepository.save(diary);
        });
    }

    @Async("appExecutor")
    @Transactional
    public CompletableFuture<Void> saveEmotionapiAsync(int diaryId, String emotion) {
        return CompletableFuture.runAsync(() -> {
            Emotionapi e = new Emotionapi();
            e.setDiaryId(diaryId);
            e.setEmotion(emotion);
            emotionapiRepository.save(e);
        });
    }

    @Async("appExecutor")
    @Transactional
    public CompletableFuture<Boolean> deleteDiaryAsync(int diaryId, int userId) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Diary> opt = diaryRepository.findById(diaryId);
            if (opt.isEmpty()) return false;
            Diary diary = opt.get();
            if (diary.getUserId() != userId) return false;
            diaryRepository.deleteById(diaryId);
            return true;
        }).thenApply(deleted -> {
            if (deleted) {
                var ctx = org.springframework.web.context.ContextLoader.getCurrentWebApplicationContext();
                if (ctx != null) {
                    var cm = ctx.getBean(org.springframework.cache.CacheManager.class);
                    var cache = cm.getCache("diariesByUser");
                    if (cache != null) cache.evict(userId);
                }
            }
            return deleted;
        });
    }

    public Optional<Emotionapi> getEmotionByDiaryId(int diaryId) {
        return emotionapiRepository.findByDiaryId(diaryId);
    }
}

