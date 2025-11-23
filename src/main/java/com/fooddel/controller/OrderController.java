package com.fooddel.controller;

import com.fooddel.dto.request.OrderRequest;
import com.fooddel.dto.response.ApiResponse;
import com.fooddel.dto.response.OrderResponse;
import com.fooddel.dto.response.PaginatedResponse;
import com.fooddel.entity.Order;
import com.fooddel.entity.User;
import com.fooddel.exception.custom.BusinessException;
import com.fooddel.mapper.OrderMapper;
import com.fooddel.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 注文関連のAPIエンドポイントを処理するコントローラー
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    /**
     * 新しい注文を作成します。
     * @param user 認証済みユーザー
     * @param orderRequest 注文作成リクエスト
     * @return 作成された注文情報
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody OrderRequest orderRequest
    ) {
        Order createdOrder = orderService.createOrder(user.getId(), orderRequest);
        OrderResponse orderResponse = orderMapper.toResponse(createdOrder);
        ApiResponse<OrderResponse> response = ApiResponse.success(orderResponse, "注文が正常に作成されました。");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * 認証済みユーザーの注文履歴をページネーション付きで取得します。
     * @param user 認証済みユーザー
     * @param pageable ページネーション情報
     * @return 注文のページ
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal User user,
            Pageable pageable
    ) {
        Page<Order> orderPage = orderService.findOrdersByUserId(user.getId(), pageable);
        return createPaginatedOrderResponse(orderPage, "注文履歴が正常に取得されました。");
    }

    /**
     * 指定されたIDの注文を取得します。
     * @param user 認証済みユーザー
     * @param id 注文ID
     * @return 注文情報
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @AuthenticationPrincipal User user,
            @PathVariable Integer id
    ) {
        return orderService.findOrderById(id)
                .map(order -> {
                    // 自分の注文かどうかを確認
                    if (!order.getUser().getId().equals(user.getId())) {
                        throw new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "この注文にアクセスする権限がありません。");
                    }
                    return orderMapper.toResponse(order);
                })
                .map(orderResponse -> ApiResponse.success(orderResponse, "注文が正常に取得されました。"))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "指定された注文が見つかりません。"));
    }

    /**
     * 指定されたIDの注文をキャンセルします。
     * @param user 認証済みユーザー
     * @param id 注文ID
     * @return キャンセルされた注文情報
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @AuthenticationPrincipal User user,
            @PathVariable Integer id
    ) {
        // 自分の注文かどうかを確認
        orderService.findOrderById(id)
                .ifPresent(order -> {
                    if (!order.getUser().getId().equals(user.getId())) {
                        throw new BusinessException(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "この注文をキャンセルする権限がありません。");
                    }
                });

        Order cancelledOrder = orderService.cancelOrder(id);
        OrderResponse orderResponse = orderMapper.toResponse(cancelledOrder);
        ApiResponse<OrderResponse> response = ApiResponse.success(orderResponse, "注文が正常にキャンセルされました。");
        return ResponseEntity.ok(response);
    }

    // ページネーションレスポンスを生成するヘルパーメソッド
    private ResponseEntity<ApiResponse<PaginatedResponse<OrderResponse>>> createPaginatedOrderResponse(Page<Order> orderPage, String message) {
        List<OrderResponse> orderResponses = orderPage.getContent().stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());

        PaginatedResponse<OrderResponse> paginatedData = new PaginatedResponse<>(
                orderResponses,
                new com.fooddel.dto.response.PaginationInfo(
                        orderPage.getNumber(),
                        orderPage.getSize(),
                        orderPage.getTotalElements(),
                        orderPage.getTotalPages(),
                        orderPage.hasNext(),
                        orderPage.hasPrevious()
                )
        );

        ApiResponse<PaginatedResponse<OrderResponse>> response = ApiResponse.success(paginatedData, message);
        return ResponseEntity.ok(response);
    }
}
