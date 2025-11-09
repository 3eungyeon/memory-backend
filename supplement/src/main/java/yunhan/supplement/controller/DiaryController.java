
package yunhan.supplement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import yunhan.supplement.Entity.Diary;
import yunhan.supplement.Entity.Emotionapi;
import yunhan.supplement.Entity.UserEntity;
import yunhan.supplement.Repository.UserRepository;
import yunhan.supplement.Service.*;
import yunhan.supplement.DTO.DiaryDTO;
import yunhan.supplement.util.EmotionParser;
import yunhan.supplement.util.TranslationParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final FirebaseStorageService firebaseStorageService;
    private final DiaryService diaryService;
    private final UserRepository userRepository;
    private final TwinwordService twinwordService;
    private final GoogleTranslateService translateService;
    private final ObjectMapper objectMapper; // JSON 문자열 변환용

    @PostMapping(value = "/diary", consumes = {"multipart/form-data"})
    public CompletableFuture<ResponseEntity<Map<String, Object>>> uploadAndCreateDiary(
            Authentication authentication,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestPart(value = "imageUrl", required = false) MultipartFile image,
            @RequestParam("weather") String weather,
            @RequestParam("date") String date) {

        if (authentication == null) {
            return CompletableFuture.completedFuture(ResponseEntity.status(403)
                    .body(Map.of("message", "JWT 인증 필요")));
        }

        String username = authentication.getName();
        Optional<UserEntity> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return CompletableFuture.completedFuture(ResponseEntity.status(404)
                    .body(Map.of("message", "User not found")));
        }

        int userId = userOptional.get().getId();

        CompletableFuture<String> imageUrlFuture = (image != null && !image.isEmpty())
                ? firebaseStorageService.uploadImageAsync(image)
                : CompletableFuture.completedFuture(null);

        return imageUrlFuture.thenCompose(imageUrl ->
                diaryService.saveDiaryAsync(userId, title, content, imageUrl, weather, date)
                        .thenApply(v -> {
                            Map<String, Object> response = new HashMap<>();
                            response.put("message", "Diary created successfully");
                            if (imageUrl != null) response.put("imageUrl", imageUrl);
                            return ResponseEntity.ok(response);
                        })
        );
    }

    /** ✅ 프론트가 문자열을 기대하는 경우: JSON을 문자열로 변환해서 내려줌 */
    @GetMapping(value = "/my", produces = "text/plain;charset=UTF-8")
    public CompletableFuture<ResponseEntity<String>> getMyDiaries(Authentication authentication) {
        if (authentication == null) {
            return CompletableFuture.completedFuture(ResponseEntity.status(403).body(""));
        }
        String username = authentication.getName();
        Optional<UserEntity> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return CompletableFuture.completedFuture(ResponseEntity.status(404).body(""));
        }
        int userId = userOptional.get().getId();

        return diaryService.getDiariesByUserIdAsync(userId).thenApply(myDiaries -> {
            List<DiaryDTO> dtos = myDiaries.stream()
                    .map(d -> new DiaryDTO(d.getDiaryId(), d.getTitle(), d.getDate()))
                    .collect(Collectors.toList());
            try {
                String json = objectMapper.writeValueAsString(dtos); // JSON 문자열로 변환
                return ResponseEntity.ok(json);
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body("");
            }
        });
    }

    @GetMapping("/diary/{diaryId}")
    public ResponseEntity<Diary> getDiaryById(@PathVariable int diaryId) {
        return diaryService.getDiaryById(diaryId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body(null));
    }


    private static String ensureUtf8Decoded(String s) {
        if (s == null) return null;
        // %EC%... 패턴 있으면 UTF-8로 디코딩
        if (s.matches("(?i).*(%[0-9a-f]{2})+.*")) {
            try {
                return java.net.URLDecoder.decode(s, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception ignore) {}
        }
        return s;
    }

    @GetMapping("/diary/{diaryId}/emotion")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> emotion(@PathVariable int diaryId) {
        Optional<Diary> diaryOptional = diaryService.getDiaryById(diaryId);
        if (diaryOptional.isEmpty()) {
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(404).body(Map.of("message", "다이어리를 찾을 수 없습니다."))
            );
        }

        Optional<Emotionapi> existing = diaryService.getEmotionByDiaryId(diaryId);
        if (existing.isPresent()) {
            // 저장값이 URL 인코딩돼 있으면 정규화해서 반환
            String normalized = ensureUtf8Decoded(existing.get().getEmotion());
            return CompletableFuture.completedFuture(
                    ResponseEntity.ok(Map.of("diaryId", diaryId, "emotion", normalized))
            );
        }

        String content = diaryOptional.get().getContent();
        if (content == null) content = "";


        return translateService.translateText(content, "en")
                .thenApply(translated -> {
                    System.out.println("[emotion] translated_len=" + (translated == null ? -1 : translated.length()));
                    System.out.println("[emotion] translated_head=" + (translated == null ? null : translated.substring(0, Math.min(80, translated.length()))));
                    return translated; // 로깅 후 그대로 다음 단계로 전달
                })
                .thenCompose(twinwordService::analyzeEmotionAsync)
                .thenApply(EmotionParser::extractKeywords)
                .thenCompose(list -> {
                    if (list.isEmpty()) {
                        String finalEmotion = "중립";
                        return diaryService.saveEmotionapiAsync(diaryId, finalEmotion)
                                .thenApply(v -> ResponseEntity.ok(Map.of("diaryId", diaryId, "emotion", finalEmotion)));
                    }
                    String finalEmotion = ensureUtf8Decoded(list.get(0)).trim();
                    return diaryService.saveEmotionapiAsync(diaryId, finalEmotion)
                            .thenApply(v -> ResponseEntity.ok(Map.of("diaryId", diaryId, "emotion", finalEmotion)));
                });


//        return translateService.translateText(content, "en")
//                .thenApply(translatedRaw -> {
//                    String extracted = TranslationParser.extractTranslatedText(translatedRaw);
//                    if (extracted == null || extracted.isBlank()) {
//                        System.out.println("[emotion] extractTranslatedText blank -> fall back to raw translation");
//                        return translatedRaw; // 폴백!
//                    }
//                    return extracted;
//                })
//                .thenCompose(twinwordService::analyzeEmotionAsync)
//                .thenApply(EmotionParser::extractKeywords)
//                .thenCompose(list -> {
//                    if (list.isEmpty()) {
//                        // 원하면 중립 폴백
//                        String finalEmotion = "중립";
//                        return diaryService.saveEmotionapiAsync(diaryId, finalEmotion)
//                                .thenApply(v -> ResponseEntity.ok(Map.of("diaryId", diaryId, "emotion", finalEmotion)));
//                    }
//                    String finalEmotion = ensureUtf8Decoded(list.get(0)).trim();
//                    return diaryService.saveEmotionapiAsync(diaryId, finalEmotion)
//                            .thenApply(v -> ResponseEntity.ok(Map.of("diaryId", diaryId, "emotion", finalEmotion)));
//                });
    }

    @DeleteMapping("/diary/{diaryId}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> deleteDiary(Authentication authentication,
                                                                              @PathVariable int diaryId) {
        if (authentication == null) {
            return CompletableFuture.completedFuture(ResponseEntity.status(403)
                    .body(Map.of("message", "JWT 인증 필요")));
        }
        String username = authentication.getName();
        Optional<UserEntity> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return CompletableFuture.completedFuture(ResponseEntity.status(404)
                    .body(Map.of("message", "User not found")));
        }
        int userId = userOptional.get().getId();
        return diaryService.deleteDiaryAsync(diaryId, userId)
                .thenApply(isDeleted -> isDeleted
                        ? ResponseEntity.ok(Map.of("message", "다이어리 삭제 성공"))
                        : ResponseEntity.status(403)
                        .body(Map.of("message", "삭제할 권한이 없거나 다이어리가 존재하지 않습니다.")));
    }

    @GetMapping(value = "/diary/{diaryId}/emotion/debug-deep", produces = "application/json")
    public ResponseEntity<Map<String,Object>> emotionDebugDeep(@PathVariable int diaryId) {
        var out = new LinkedHashMap<String, Object>();
        try {
            var dOpt = diaryService.getDiaryById(diaryId);
            if (dOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("message", "no diary"));
            }

            String content = dOpt.get().getContent();
            int contentLen = (content == null) ? -1 : content.length();
            out.put("content_len", contentLen);
            out.put("content_head", content == null ? null : content.substring(0, Math.min(80, content.length())));

            String translated = null;
            String translateErr = null;
            try {
                translated = translateService.translateText((content == null) ? "" : content, "en").join();
            } catch (Exception te) {
                translateErr = te.getClass().getSimpleName() + ": " + te.getMessage();
            }
            out.put("translated_len", translated == null ? -1 : translated.length());
            out.put("translate_error", translateErr);

            String raw = null;
            String twinwordStatus = "OK";
            String twinwordErr = null;
            try {
                raw = twinwordService.analyzeEmotion((translated == null) ? "" : translated).join();
            } catch (Exception e) {
                twinwordStatus = "EXCEPTION";
                twinwordErr = e.getClass().getSimpleName() + ": " + e.getMessage();
            }
            out.put("twinword_status", twinwordStatus);
            out.put("twinword_error", twinwordErr);
            out.put("twinword_raw_head", raw == null ? null : raw.substring(0, Math.min(240, raw.length())));

            List<String> parsed = Collections.emptyList();
            try {
                parsed = EmotionParser.extractKeywords(raw == null ? "" : raw);
            } catch (Exception pe) {
                out.put("parse_error", pe.getClass().getSimpleName() + ": " + pe.getMessage());
            }
            String picked = (parsed == null || parsed.isEmpty()) ? null : parsed.get(0);
            out.put("parsed", parsed);
            out.put("picked", picked);

            return ResponseEntity.ok(out);

        } catch (Exception fatal) {
            out.put("fatal_error", fatal.getClass().getSimpleName() + ": " + fatal.getMessage());
            return ResponseEntity.ok(out);
        }
    }
}
