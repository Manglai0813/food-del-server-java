package com.fooddel.repository;

import com.fooddel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ユーザーエンティティ用のリポジトリインターフェース
 * Spring Data JPAが実行時に実装を自動生成します。
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * メールアドレスでユーザーを検索します。
     * @param email 検索するメールアドレス
     * @return ユーザー（オプショナル）
     */
    Optional<User> findByEmail(String email);

    /**
     * リフレッシュトークンでユーザーを検索します。
     * @param refreshToken 検索するリフレッシュトークン
     * @return ユーザー（オプショナル）
     */
    Optional<User> findByRefreshToken(String refreshToken);
}
