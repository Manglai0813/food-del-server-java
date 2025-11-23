package com.fooddel.repository;

import com.fooddel.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 注文アイテムエンティティ用のリポジトリインターフェース
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
}
