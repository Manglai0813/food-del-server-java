package com.fooddel.repository;

import com.fooddel.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 注文ステータス履歴エンティティ用のリポジトリインターフェース
 */
@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Integer> {
}
