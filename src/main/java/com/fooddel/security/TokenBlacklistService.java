package com.fooddel.security;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * JWTトークンのブラックリストを管理するサービス（Redis実装）
 */
@Service
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    public TokenBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * トークンをブラックリストに追加します。
     * @param jti トークンの一意のID
     * @param expirationSeconds トークンの残り有効期間（秒）
     */
    public void blacklistToken(String jti, long expirationSeconds) {
        // RedisにJTIをキーとして保存し、有効期限を設定
        redisTemplate.opsForValue().set(jti, "blacklisted", Duration.ofSeconds(expirationSeconds));
    }

    /**
     * トークンがブラックリストに含まれているかを確認します。
     * @param jti トークンの一意のID
     * @return ブラックリストに含まれている場合はtrue
     */
    public boolean isTokenBlacklisted(String jti) {
        // RedisにJTIが存在するかを確認
        return Boolean.TRUE.equals(redisTemplate.hasKey(jti));
    }
}
