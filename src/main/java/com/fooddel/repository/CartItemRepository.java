package com.fooddel.repository;

import com.fooddel.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * カートアイテムエンティティ用のリポジトリインターフェース
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    /**
     * カートIDと商品IDでカートアイテムを検索します。
     * @param cartId カートID
     * @param foodId 商品ID
     * @return カートアイテム（オプショナル）
     */
    Optional<CartItem> findByCartIdAndFoodId(Integer cartId, Integer foodId);

    /**
     * 指定されたカートIDに属する全てのカートアイテムを削除します。
     * @param cartId カートID
     */
    void deleteAllByCartId(Integer cartId);
}
