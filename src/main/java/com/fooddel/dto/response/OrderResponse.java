package com.fooddel.dto.response;

import com.fooddel.constant.enums.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 注文情報レスポンス用のDTO
 */
@Data
public class OrderResponse {
    private Integer id;
    private UserResponse user; // ユーザー情報
    private LocalDateTime orderDate;
    private Double totalAmount;
    private OrderStatus status;
    private String deliveryAddress;
    private List<OrderItemResponse> orderItems; // 注文アイテムリスト
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
