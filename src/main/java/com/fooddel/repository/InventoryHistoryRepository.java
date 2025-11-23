package com.fooddel.repository;

import com.fooddel.entity.InventoryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 在庫履歴エンティティ用のリポジトリインターフェース
 */
@Repository
public interface InventoryHistoryRepository extends JpaRepository<InventoryHistory, Integer> {
}
