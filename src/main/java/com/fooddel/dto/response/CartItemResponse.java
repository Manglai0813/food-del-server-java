package com.fooddel.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * カートアイテム情報レスポンス用のDTO
 */
@Data
public class CartItemResponse {
    private Integer id;
    private FoodResponse food; // 商品情報
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
