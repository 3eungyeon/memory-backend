
package yunhan.supplement.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;
import yunhan.supplement.Entity.TimeCapsule;
import yunhan.supplement.Entity.UserEntity;
import yunhan.supplement.Repository.TimeCapsuleRepository;
import yunhan.supplement.Repository.UserRepository;
import yunhan.supplement.DTO.TimeCapsuleSummaryDTO;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
@Service
public class TimeCapsuleService {
    private final TimeCapsuleRepository timeCapsuleRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration OPENED_CACHE_TTL = Duration.ofHours(24);

    @Autowired
    public TimeCapsuleService(TimeCapsuleRepository timeCapsuleRepository,
                              UserRepository userRepository,
                              RedisTemplate<String, Object> redisTemplate) {
        this.timeCapsuleRepository = timeCapsuleRepository;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }

    @Async("appExecutor")
    @Transactional
    public CompletableFuture<Integer> saveTimeCapsuleAsync(int creatorId, String title, String content, String imageUrl, String openDate, List<Integer> userIds) {
        return CompletableFuture.supplyAsync(() -> {
            TimeCapsule tc = new TimeCapsule();
            tc.setTitle(title);
            tc.setContent(content);
            tc.setImagePath(imageUrl);
            tc.setOpenDate(LocalDateTime.parse(openDate));
            tc.setIsOpened(false);

            Set<UserEntity> users = new HashSet<>();
            userRepository.findById(creatorId).ifPresent(users::add);
            for (int id : userIds) userRepository.findById(id).ifPresent(users::add);
            tc.setUsers(new ArrayList<>(users));

            int id = timeCapsuleRepository.save(tc).getTimecapsuleId();
            redisTemplate.delete("tc:detail:" + id); // 캐시 제거
            return id;
        });
    }

    @Async("appExecutor")
    public CompletableFuture<List<TimeCapsuleSummaryDTO>> getUserTimeCapsulesAsync(int userId) {
        return CompletableFuture.supplyAsync(() -> {
            UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            return timeCapsuleRepository.findByUsersContains(user).stream()
                    .map(tc -> new TimeCapsuleSummaryDTO(tc.getTimecapsuleId(), tc.getTitle(), tc.getOpenDate(), tc.getIsOpened()))
                    .collect(Collectors.toList());
        });
    }

    public Map<String, Object> getTimeCapsuleDetail(int timecapsuleId) {
        String key = "tc:detail:" + timecapsuleId;

        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) cached;
            return m;
        }

        TimeCapsule tc = timeCapsuleRepository.findById(timecapsuleId)
                .orElseThrow(() -> new RuntimeException("Time capsule not found"));

        LocalDateTime now = LocalDateTime.now();
        if (!tc.getIsOpened() && tc.getOpenDate().isBefore(now)) {
            tc.setIsOpened(true);
            timeCapsuleRepository.save(tc);
        }

        Map<String, Object> res;
        if (!tc.getIsOpened()) {
            res = Map.of("message", "아직 기한이 되지 않았습니다.");
            redisTemplate.opsForValue().set(key, res);
            redisTemplate.expireAt(key, java.sql.Timestamp.valueOf(tc.getOpenDate())); // openDate에 자동 만료
            return res;
        }

        List<UserEntity> users = tc.getUsers();
        List<String> usernames = users.stream().map(UserEntity::getUsername).toList();
        List<String> names = users.stream().map(UserEntity::getName).toList();
        res = new HashMap<>();
        res.put("timecapsuleId", tc.getTimecapsuleId());
        res.put("title", tc.getTitle());
        res.put("content", tc.getContent() != null ? tc.getContent() : "");
        res.put("imagePath", tc.getImagePath() != null ? tc.getImagePath() : "");
        res.put("openDate", tc.getOpenDate());
        res.put("isOpened", tc.getIsOpened());
        res.put("usernames", usernames);
        res.put("names", names);

        redisTemplate.opsForValue().set(key, res, OPENED_CACHE_TTL); // 열린 후에는 일반 TTL
        return res;
    }

    @Async("appExecutor")
    @Transactional
    public CompletableFuture<Void> deleteTimeCapsuleAsync(int timecapsuleId) {
        return CompletableFuture.runAsync(() -> {
            if (!timeCapsuleRepository.existsById(timecapsuleId)) {
                throw new RuntimeException("Time capsule not found");
            }
            timeCapsuleRepository.deleteById(timecapsuleId);
            redisTemplate.delete("tc:detail:" + timecapsuleId);
        });
    }
}
