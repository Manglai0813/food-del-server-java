package com.fooddel.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * カート情報レスポンス用のDTO
 */
@Data
public class CartResponse {
    private Integer id;
    private UserResponse user; // ユーザー情報
    private List<CartItemResponse> cartItems; // カートアイテムリスト
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
