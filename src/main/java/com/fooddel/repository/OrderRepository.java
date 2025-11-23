package com.fooddel.repository;

import com.fooddel.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 注文エンティティ用のリポジトリインターフェース
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    /**
     * ユーザーIDで注文を検索し、ページネーション付きで返します。
     * 
     * @param userId   ユーザーID
     * @param pageable ページネーション情報
     * @return 注文のページ
     */
    Page<Order> findByUserId(Integer userId, Pageable pageable);

    /**
     * 指定されたユーザーIDの注文が存在するかチェックします。
     * 
     * @param userId ユーザーID
     * @return 存在する場合はtrue
     */
    boolean existsByUserId(Integer userId);
}
