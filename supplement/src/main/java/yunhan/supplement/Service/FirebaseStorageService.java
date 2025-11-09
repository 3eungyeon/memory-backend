


package yunhan.supplement.Service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class FirebaseStorageService {

    private final Storage storage;
    private final String bucketName = "supplement-33176.firebasestorage.app";

    public FirebaseStorageService() throws IOException {
        // ✅ 환경변수로부터 Firebase 서비스 계정 키 경로 읽기
        String credPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (credPath == null || credPath.isBlank()) {
            throw new IllegalStateException("GOOGLE_APPLICATION_CREDENTIALS 환경변수가 비어 있습니다.");
        }

        File credFile = new File(credPath);
        if (!credFile.exists()) {
            throw new FileNotFoundException("Firebase 자격증명 파일을 찾을 수 없습니다: " + credFile.getAbsolutePath());
        }

        try (InputStream serviceAccount = new FileInputStream(credFile)) {
            this.storage = StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build()
                    .getService();
        }
    }

    /**
     * Firebase Storage에 이미지를 업로드하고 다운로드 가능한 URL을 반환
     */
    @Async("appExecutor")
    public CompletableFuture<String> uploadImageAsync(MultipartFile file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String originalName = file.getOriginalFilename();
                String safeOriginal = (originalName == null) ? "file" : originalName;
                String fileName = UUID.randomUUID() + "_" + safeOriginal;

                Bucket bucket = storage.get(bucketName);
                if (bucket == null) {
                    throw new IllegalStateException("버킷을 찾을 수 없습니다: " + bucketName);
                }

                String downloadToken = UUID.randomUUID().toString();

                BlobInfo blobInfo = BlobInfo.newBuilder(bucket.getName(), fileName)
                        .setContentType(file.getContentType())
                        .setMetadata(java.util.Map.of("firebaseStorageDownloadTokens", downloadToken))
                        .build();

                bucket.create(fileName, file.getInputStream(), blobInfo.getContentType());

                String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
                return "https://firebasestorage.googleapis.com/v0/b/" + bucketName +
                        "/o/" + encodedName + "?alt=media&token=" + downloadToken;

            } catch (IOException e) {
                throw new RuntimeException("Image upload failed: " + e.getMessage(), e);
            }
        });
    }
}
