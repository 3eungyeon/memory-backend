
package yunhan.supplement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import yunhan.supplement.DTO.TimeCapsuleSummaryDTO;
import yunhan.supplement.Entity.UserEntity;
import yunhan.supplement.Repository.UserRepository;
import yunhan.supplement.Service.FirebaseStorageService;
import yunhan.supplement.Service.TimeCapsuleService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/timecapsules")
@RequiredArgsConstructor
public class TimeCapsuleController {
    private final FirebaseStorageService firebaseStorageService;
    private final TimeCapsuleService timeCapsuleService;
    private final UserRepository userRepository;

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<ResponseEntity<Map<String, Object>>> createTimeCapsule(
            Authentication authentication,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestParam("openDate") String openDate,
            @RequestParam("userIds") List<Integer> userIds) {

        if (authentication == null) {
            return CompletableFuture.completedFuture(ResponseEntity.status(403).body(Map.of("message", "JWT 인증 필요")));
        }
        String username = authentication.getName();
        Optional<UserEntity> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return CompletableFuture.completedFuture(ResponseEntity.status(404).body(Map.of("message", "User not found")));
        }
        int userId = userOptional.get().getId();

        CompletableFuture<String> imageUrlFuture = (image != null && !image.isEmpty())
                ? firebaseStorageService.uploadImageAsync(image)
                : CompletableFuture.completedFuture(null);

        return imageUrlFuture.thenCompose(imageUrl ->
                timeCapsuleService.saveTimeCapsuleAsync(userId, title, content, imageUrl, openDate, userIds)
                        .thenApply(id -> {
                            Map<String, Object> res = new HashMap<>();
                            res.put("message", "Time capsule created successfully");
                            res.put("timecapsuleId", id);
                            if (imageUrl != null) res.put("imageUrl", imageUrl);
                            return ResponseEntity.ok(res);
                        }));
    }

    @GetMapping("/{userId}")
    public CompletableFuture<ResponseEntity<?>> getUserTimeCapsules(@PathVariable int userId) {
        return timeCapsuleService.getUserTimeCapsulesAsync(userId)
                .<ResponseEntity<?>>thenApply(ResponseEntity::ok)
                .exceptionally(e -> ResponseEntity.status(404).body(Map.of("message", e.getMessage())));
    }

    @GetMapping("/detail/{timecapsuleId}")
    public ResponseEntity<Map<String, Object>> getTimeCapsuleDetail(@PathVariable int timecapsuleId) {
        return ResponseEntity.ok(timeCapsuleService.getTimeCapsuleDetail(timecapsuleId));
    }

    @DeleteMapping("/{timecapsuleId}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> deleteTimeCapsule(@PathVariable int timecapsuleId) {
        return timeCapsuleService.deleteTimeCapsuleAsync(timecapsuleId)
                .thenApply(v -> ResponseEntity.ok(Map.<String, Object>of("message", "Time capsule deleted successfully")))
                .exceptionally(e -> ResponseEntity.status(404).body(Map.<String, Object>of("message", e.getMessage())));
    }
}