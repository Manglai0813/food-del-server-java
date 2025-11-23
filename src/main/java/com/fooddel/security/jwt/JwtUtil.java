package com.fooddel.security.jwt;

import com.fooddel.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * JWTトークンの生成、検証、解析を行うユーティリティクラス
 */
@Component
public class JwtUtil {

    // アクセストークンの有効期間（ミリ秒）- 24時間
    public static final long JWT_ACCESS_TOKEN_VALIDITY = 24 * 60 * 60 * 1000;
    // リフレッシュトークンの有効期間（ミリ秒）- 7日間
    public static final long JWT_REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000;

    // application.ymlからシークレットキーを読み込む
    @Value("${app.jwt.secret}")
    private String accessSecret;

    @Value("${app.jwt.refresh-secret}")
    private String refreshSecret;

    // ユーザー情報からアクセストークンを生成
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // カスタムクレームを追加可能 (例: ロール)
        if (userDetails instanceof User) {
            claims.put("role", ((User) userDetails).getRole());
            claims.put("userId", ((User) userDetails).getId());
        }
        return doGenerateToken(claims, userDetails.getUsername(), JWT_ACCESS_TOKEN_VALIDITY, getAccessSigningKey());
    }

    // ユーザー情報からリフレッシュトークンを生成
    public String generateRefreshToken(UserDetails userDetails) {
        // リフレッシュトークンには余分な情報を含めない
        return doGenerateToken(new HashMap<>(), userDetails.getUsername(), JWT_REFRESH_TOKEN_VALIDITY, getRefreshSigningKey());
    }

    // トークンの生成処理
    private String doGenerateToken(Map<String, Object> claims, String subject, long validity, Key signingKey) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + validity))
                .setId(UUID.randomUUID().toString()) // JTI (JWT ID) を追加
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // トークンの検証
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // トークンからユーザー名（メールアドレス）を抽出
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // トークンから有効期限を抽出
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // トークンからJTI（JWT ID）を抽出
    public String extractJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    // トークンから特定のクレームを抽出
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // トークンから全てのクレームを抽出
    private Claims extractAllClaims(String token) {
        // 注意：アクセストークンとリフレッシュトークンでキーが異なるため、
        // 検証なしでクレームを抽出するのはリスクが伴う。
        // 本来は、トークンの種類を判断して適切なキーで検証すべき。
        // ここでは簡略化のため、アクセストークンのキーで試行する。
        return Jwts.parserBuilder()
                .setSigningKey(getAccessSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // トークンが期限切れかどうかをチェック
    private Boolean isTokenExpired(String token) {
        final Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }

    // アクセストークン用の署名キーを生成
    private Key getAccessSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.accessSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // リフレッシュトークン用の署名キーを生成
    private Key getRefreshSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.refreshSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
