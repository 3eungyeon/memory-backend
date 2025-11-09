package yunhan.supplement.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));  // ✅ Base64 디코딩 적용
    }

    // JWT 토큰 생성
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1시간 후 만료
                .signWith(getSigningKey(), io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();
    }


    // JWT 토큰 검증 및 사용자 이름 반환
    public String validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())  // ✅ Base64 디코딩한 키 사용
                    .build()
                    .parseClaimsJws(token);

            return claims.getBody().getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("Invalid token");
        }
    }

    // JWT 토큰 만료 여부 확인
    public boolean isTokenExpired(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }

    // JWT 토큰 유효성 검사
    public boolean validateToken(String token, String username) {
        String extractedUsername = validateToken(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
}
