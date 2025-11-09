package yunhan.supplement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yunhan.supplement.Service.GoogleTranslateService;

import java.util.concurrent.CompletableFuture;
@RestController
@RequestMapping("/translate")
public class TranslationController {


    private final GoogleTranslateService translateService;


    public TranslationController(GoogleTranslateService translateService) {
        this.translateService = translateService;
    }


    @GetMapping
    public CompletableFuture<ResponseEntity<String>> translate(
            @RequestParam String text,
            @RequestParam(defaultValue = "en") String target) {


        return translateService.translateText(text, target)
                .thenApply(ResponseEntity::ok);
    }

}
