package yunhan.supplement.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import yunhan.supplement.DTO.UserDTO;
import yunhan.supplement.Entity.UserEntity;
import yunhan.supplement.Repository.UserRepository;
import yunhan.supplement.security.JwtUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    private final Set<String> verifiedUsernames = new HashSet<>();

    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    public void verifyUsername(String username) {
        if (isUsernameTaken(username)) {
            throw new IllegalArgumentException("Username is already taken");
        }
        verifiedUsernames.add(username);
    }

    public void saveUser(UserDTO userDTO) {
        if (!verifiedUsernames.contains(userDTO.getUsername())) {
            throw new IllegalStateException("You must verify the username before registration");
        }
        verifiedUsernames.remove(userDTO.getUsername());

        UserEntity userEntity = UserDTO.toEntity(userDTO);
        userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        userRepository.save(userEntity);
    }

    public Map<String, Object> login(String username, String password) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        System.out.println("DB에 저장된 해시된 비밀번호: " + user.getPassword());
        System.out.println("입력된 비밀번호: " + password);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            System.out.println("비밀번호 불일치");
            throw new IllegalArgumentException("Invalid password");
        }

        System.out.println("로그인 성공: " + username);

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwtUtil.generateToken(username));
        response.put("user_id", user.getId());

        return response;
    }

    public Optional<UserEntity> searchUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 기존 비밀번호 확인
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호가 기존 비밀번호와 같은지 확인
        if (oldPassword.equals(newPassword)) {
            throw new IllegalArgumentException("새 비밀번호가 기존 비밀번호와 같습니다. 다른 비밀번호를 사용하세요.");
        }

        // 새 비밀번호 암호화 및 저장
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private UserRepository UserRepository;

    public Optional<UserEntity> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}

