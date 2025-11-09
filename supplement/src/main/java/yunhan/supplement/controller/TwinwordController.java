package yunhan.supplement.controller;
import org.springframework.web.bind.annotation.*;
import yunhan.supplement.Service.TwinwordService;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/twinword")
@CrossOrigin("*")
public class TwinwordController {

    private final TwinwordService twinwordService;

    public TwinwordController(TwinwordService twinwordService) {
        this.twinwordService = twinwordService;
    }

    @PostMapping("/analyze")
    public CompletableFuture<String> analyzeEmotion(@RequestParam String text) {
        // ✅ 서비스 메서드 이름과 동일하게 호출
        return twinwordService.analyzeEmotionAsync(text);
    }
}
