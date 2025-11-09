
package yunhan.supplement.Service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;


@Service
public class GoogleTranslateService {
    @Value("${google.cloud.translate.api-key}")
    private String apiKey;


    private final WebClient webClient;


    public GoogleTranslateService(WebClient webClient) {
        this.webClient = webClient;
    }


    @Async("appExecutor")
    public CompletableFuture<String> translateText(String text, String targetLanguage) {
        String url = String.format(
                "https://translation.googleapis.com/language/translate/v2?key=%s&q=%s&target=%s",
                apiKey,
                URLEncoder.encode(text, StandardCharsets.UTF_8),
                URLEncoder.encode(targetLanguage, StandardCharsets.UTF_8)
        );


        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .toFuture();
    }
}
