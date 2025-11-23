package com.fooddel.service;

import com.fooddel.dto.request.OrderRequest;
import com.fooddel.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 注文関連のビジネスロジックを定義するサービスインターフェース
 */
public interface OrderService {

    // 新しい注文を作成します。
    Order createOrder(Integer userId, OrderRequest orderRequest);

    // 指定されたIDの注文を検索します。
    Optional<Order> findOrderById(Integer orderId);

    // 指定されたユーザーの注文をページネーション付きで取得します。
    Page<Order> findOrdersByUserId(Integer userId, Pageable pageable);

    // 注文をキャンセルします。
    Order cancelOrder(Integer orderId);

    // 全ての注文をページネーション付きで取得します（管理者用）。
    Page<Order> findAllOrders(Pageable pageable);
}
