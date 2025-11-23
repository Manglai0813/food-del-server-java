package com.fooddel.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 注文アイテム情報レスポンス用のDTO
 */
@Data
public class OrderItemResponse {
    private Integer id;
    private FoodResponse food; // 商品情報
    private Integer quantity;
    private Double price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
