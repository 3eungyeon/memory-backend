package yunhan.supplement.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

public class EmotionParser {

    private static final Map<String, String> KO_LABELS = Map.of(
            "joy", "기쁨",
            "surprise", "놀람",
            "disgust", "싫음",
            "sadness", "슬픔",
            "anger", "분노",
            "fear", "두려움"
    );

    /**
     * Twinword raw JSON -> [한글 감정 한 개] (최고 점수)
     * 비정상/빈 경우는 빈 리스트 반환
     */
    public static List<String> extractKeywords(String json) {
        List<String> out = new ArrayList<>();
        if (json == null || json.isBlank()) return out;

        try {
            // "emotion_scores": { ... } 본문만 추출
            var block = java.util.regex.Pattern.compile(
                    "\"emotion_scores\"\\s*:\\s*\\{([^}]*)\\}",
                    java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL);
            var bm = block.matcher(json);
            if (!bm.find()) return out;
            String scoresBody = bm.group(1);

            // key:value 쌍 추출 (정수/소수/과학표기 모두)
            var kv = java.util.regex.Pattern.compile(
                    "\"(surprise|joy|sadness|disgust|anger|fear)\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?)");
            var mkv = kv.matcher(scoresBody);

            Map<String, java.math.BigDecimal> scores = new java.util.HashMap<>();
            while (mkv.find()) {
                String key = mkv.group(1);
                String num = mkv.group(2);
                try { scores.put(key, new java.math.BigDecimal(num)); } catch (Exception ignore) {}
            }
            if (scores.isEmpty()) return out;

            // 최대값 (정밀 비교)
// 매우 작은 감정값도 감지하도록 스케일 조정
// ... scores 채운 직후
            scores.replaceAll((k, v) -> v.multiply(new java.math.BigDecimal("100000"))); // 또는 1000000

            var max = scores.values().stream().max(java.math.BigDecimal::compareTo)
                    .orElse(java.math.BigDecimal.ZERO);

// 스케일 이후에도 여전히 모두 0이면 진짜 0으로 판단
            if (max.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return out; // 또는 "중립" 폴백을 원하면 여기서 out.add("중립")
            }

            // 거의 같은 값(동점)만 우선순위로
            final var EPS = new java.math.BigDecimal("0.0000005"); // 5e-7
            var candidates = scores.entrySet().stream()
                    .filter(e -> e.getValue().subtract(max).abs().compareTo(EPS) <= 0)
                    .map(Map.Entry::getKey).toList();

            String picked;
            if (candidates.size() == 1) {
                picked = candidates.get(0);
            } else {
                // 동점일 때만 우선순위 (맘대로 조정 가능)
                var priority = java.util.List.of("joy", "surprise", "sadness", "anger", "fear", "disgust");
                picked = candidates.stream()
                        .sorted((a,b) -> {
                            int ai = priority.indexOf(a), bi = priority.indexOf(b);
                            if (ai == -1 && bi == -1) return a.compareTo(b);
                            if (ai == -1) return 1;
                            if (bi == -1) return -1;
                            return Integer.compare(ai, bi);
                        })
                        .findFirst().orElse(null);
            }

//            System.out.println("[EmotionParser] raw=" + scores + ", max=" + max + ", candidates=" + candidates + ", picked=" + picked);

            if (picked != null) out.add(KO_LABELS.getOrDefault(picked, picked)); // en→ko 매핑
        } catch (Exception e) {
//            System.out.println("[EmotionParser] parse error: " + e.getMessage());
        }
        return out;
    }




}

