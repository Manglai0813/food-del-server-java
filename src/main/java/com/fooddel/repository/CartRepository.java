package com.fooddel.repository;

import com.fooddel.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * カートエンティティ用のリポジトリインターフェース
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    /**
     * ユーザーIDでカートを検索します。
     * @param userId 検索するユーザーID
     * @return カート（オプショナル）
     */
    Optional<Cart> findByUserId(Integer userId);
}
