package yunhan.supplement.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import yunhan.supplement.DTO.UserDTO;
import yunhan.supplement.Entity.UserEntity;
import yunhan.supplement.Service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/check-username")
    public ResponseEntity<Map<String, String>> checkUsername(@RequestBody UserDTO userDTO) {
        Map<String, String> response = new HashMap<>();
        try {
            userService.verifyUsername(userDTO.getUsername());
            response.put("message", "Username is available");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/save")
    public ResponseEntity<Map<String, String>> saveUser(@RequestBody UserDTO userDTO) {
        Map<String, String> response = new HashMap<>();
        try {
            userService.saveUser(userDTO);
            response.put("message", "User registered successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserDTO userDTO) {
        Map<String, Object> response = new HashMap<>();
        try {
            response = userService.login(userDTO.getUsername(), userDTO.getPassword());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping(value="/search", produces="text/plain;charset=UTF-8")
    public ResponseEntity<String> searchUser(@RequestParam String username) {
        return userService.searchUserByUsername(username)
                .map(u -> {
                    String json = String.format("{\"id\":%d,\"username\":\"%s\",\"name\":\"%s\"}",
                            u.getId(), u.getUsername(), u.getName());
                    return ResponseEntity.ok(json);
                })
                .orElse(ResponseEntity.status(404).body("{\"message\":\"User not found\"}"));
    }



    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            Authentication authentication,
            @RequestBody Map<String, String> request) {

        Map<String, String> response = new HashMap<>();

        if (authentication == null) {
            response.put("error", "JWT 인증 필요");
            return ResponseEntity.status(403).body(response);
        }

        String username = authentication.getName();
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        try {
            userService.changePassword(username, oldPassword, newPassword);
            response.put("message", "비밀번호 변경 성공");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        // Spring Security에서 현재 사용자 이름 가져오기
        String username = authentication.getName();

        Optional<UserEntity> user = userService.getUserByUsername(username);

        if (user.isPresent()) {
            Map<String, String> response = new HashMap<>();
            response.put("username", user.get().getUsername());
            response.put("name", user.get().getName());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }
}
