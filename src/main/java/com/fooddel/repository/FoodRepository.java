package com.fooddel.repository;

import com.fooddel.entity.Food;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 商品エンティティ用のリポジトリインターフェース
 * JpaSpecificationExecutorを継承して動的なクエリをサポートします。
 */
@Repository
public interface FoodRepository extends JpaRepository<Food, Integer>, JpaSpecificationExecutor<Food> {

    /**
     * 指定されたIDの商品を悲観的ロックを取得して検索します。
     * 
     * @param id 商品ID
     * @return 商品（オプショナル）
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Food f WHERE f.id = :id")
    Optional<Food> findByIdWithLock(Integer id);

    /**
     * 指定されたカテゴリIDに属し、かつステータスが有効な商品をページネーション付きで検索します。
     * 
     * @param categoryId カテゴリID
     * @param status     商品のステータス
     * @param pageable   ページネーション情報
     * @return 商品のページ
     */
    Page<Food> findByCategoryIdAndStatus(Integer categoryId, Boolean status, Pageable pageable);

    /**
     * ステータスが有効な商品をページネーション付きで検索します。
     * 
     * @param status   商品のステータス
     * @param pageable ページネーション情報
     * @return 商品のページ
     */
    Page<Food> findByStatus(Boolean status, Pageable pageable);

    /**
     * 指定されたカテゴリIDの商品が存在するかチェックします。
     * 
     * @param categoryId カテゴリID
     * @return 存在する場合はtrue
     */
    boolean existsByCategoryId(Integer categoryId);
}
