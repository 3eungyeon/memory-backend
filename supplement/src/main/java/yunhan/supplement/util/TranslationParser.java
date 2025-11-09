package yunhan.supplement.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TranslationParser {

    public static String extractTranslatedText(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            return root.path("data")
                    .path("translations")
                    .get(0)
                    .path("translatedText")
                    .asText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
