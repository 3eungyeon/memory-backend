
package yunhan.supplement.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TwinwordService {

    private final WebClient webClient;

    public TwinwordService(WebClient webClient) {
        this.webClient = webClient;
    }

    // properties에서 읽기
    @Value("${twinword.mode:rapidapi}")               // rapidapi | direct
    private String mode;

    @Value("${twinword.rapidapi.key:}")
    private String rapidKey;

    @Value("${twinword.rapidapi.host:twinword-emotion-analysis-v1.p.rapidapi.com}")
    private String rapidHost;

    @Value("${twinword.direct.key:}")
    private String directKey;

    private static final String RAPID_URL  = "https://twinword-emotion-analysis-v1.p.rapidapi.com/analyze/";
    private static final String DIRECT_URL = "https://api.twinword.com/api/v6/emotion/analyze/";

    @Async("appExecutor")
    public CompletableFuture<String> analyzeEmotionAsync(String text) {
        //
//        System.out.println("[TwinwordService] input text len=" + (text == null ? -1 : text.length()));
        //
        WebClient.RequestBodySpec req = "direct".equalsIgnoreCase(mode)
                ? webClient.post().uri(DIRECT_URL)
                .header("X-Twaip-Key", directKey)
                : webClient.post().uri(RAPID_URL)
                .header("X-RapidAPI-Key", rapidKey)
                .header("X-RapidAPI-Host", rapidHost);

        return req
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .acceptCharset(java.nio.charset.StandardCharsets.UTF_8)
                .body(BodyInserters.fromFormData("text", text == null ? "" : text))
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .flatMap(b -> Mono.error(new IllegalStateException("Twinword error: " + b))))
                .bodyToMono(String.class)
                .doOnNext(body -> {
                    // ----------------------------
                    // 디버그 로깅 (emotion_scores만!)
                    // ----------------------------

                    // 1) result_code / msg
                    try {
                        Matcher mCode = Pattern.compile("\"result_code\"\\s*:\\s*(\\d+)").matcher(body);
                        String rc = mCode.find() ? mCode.group(1) : "NA";
                        Matcher mMsg = Pattern.compile("\"msg\"\\s*:\\s*\"([^\"]*)\"").matcher(body);
                        String msg = mMsg.find() ? mMsg.group(1) : "";
//                        System.out.println("[TwinwordService] result_code=" + rc + ", msg=\"" + msg + "\"");
                    } catch (Exception ignore) {}

                    // 2) emotion_scores 블록만 추출해서 키:값 로깅
                    try {
                        Matcher mBlock = Pattern.compile(
                                "\"emotion_scores\"\\s*:\\s*\\{([^}]*)\\}",
                                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
                        ).matcher(body);

                        if (mBlock.find()) {
                            String scoresBody = mBlock.group(1);
                            Matcher mKV = Pattern.compile(
                                    "\"(surprise|joy|sadness|disgust|anger|fear)\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?)"
                            ).matcher(scoresBody);

                            Map<String, String> map = new LinkedHashMap<>();
                            while (mKV.find()) {
                                map.put(mKV.group(1), mKV.group(2));
                            }
//                            System.out.println("[TwinwordService] emotion_scores = " + map);
                        } else {
//                            System.out.println("[TwinwordService] emotion_scores not found");
                        }
                    } catch (Exception e) {
//                        System.out.println("[TwinwordService] log parse error: " + e.getMessage());
                    }
                })
                .timeout(Duration.ofSeconds(10))
                .toFuture();
    }

    // (호환용) 기존 analyzeEmotion 이름을 호출해도 동작
    @Async("appExecutor")
    public CompletableFuture<String> analyzeEmotion(String text) {
        return analyzeEmotionAsync(text);
    }
}
