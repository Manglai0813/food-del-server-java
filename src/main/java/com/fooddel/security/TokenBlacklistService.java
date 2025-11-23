package com.fooddel.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * JWTトークンのブラックリストを管理するサービス（Redis実装）
 * Redis接続エラー時の耐障害性を持つ
 */
@Service
@Slf4j
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;

    public TokenBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * トークンをブラックリストに追加します。
     * Redis接続エラー時はログを記録して処理を継続します。
     * @param jti トークンの一意のID
     * @param expirationSeconds トークンの残り有効期間（秒）
     */
    public void blacklistToken(String jti, long expirationSeconds) {
        try {
            // RedisにJTIをキーとして保存し、有効期限を設定
            redisTemplate.opsForValue().set(jti, "blacklisted", Duration.ofSeconds(expirationSeconds));
            log.debug("トークンをブラックリストに追加しました: {}", jti);
        } catch (RedisConnectionFailureException e) {
            // Redis接続エラー時はログを記録して処理を継続（フェイルソフト）
            log.error("Redis接続エラー: トークンのブラックリスト追加に失敗しました。JTI: {}, エラー: {}", jti, e.getMessage());
        } catch (Exception e) {
            // その他のエラーもログを記録
            log.error("予期せぬエラー: トークンのブラックリスト追加に失敗しました。JTI: {}", jti, e);
        }
    }

    /**
     * トークンがブラックリストに含まれているかを確認します。
     * Redis接続エラー時はfalseを返し、処理を継続します（フェイルオープン）。
     * @param jti トークンの一意のID
     * @return ブラックリストに含まれている場合はtrue、Redis接続エラー時はfalse
     */
    public boolean isTokenBlacklisted(String jti) {
        try {
            // RedisにJTIが存在するかを確認
            Boolean result = redisTemplate.hasKey(jti);
            return Boolean.TRUE.equals(result);
        } catch (RedisConnectionFailureException e) {
            // Redis接続エラー時はログを記録してfalseを返す（フェイルオープン）
            // 注意: セキュリティ要件が厳しい場合は、trueを返す（フェイルクローズ）ことも検討
            log.warn("Redis接続エラー: ブラックリストチェックをスキップします。JTI: {}, エラー: {}", jti, e.getMessage());
            return false;
        } catch (Exception e) {
            // その他のエラーもログを記録してfalseを返す
            log.error("予期せぬエラー: ブラックリストチェックに失敗しました。JTI: {}", jti, e);
            return false;
        }
    }
}
