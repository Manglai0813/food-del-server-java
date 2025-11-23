package com.fooddel.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * カートアイテム追加・更新リクエスト用のDTO
 */
@Data
public class CartItemRequest {

    @NotNull(message = "商品IDは必須です")
    private Integer foodId;

    @NotNull(message = "数量は必須です")
    @Min(value = 1, message = "数量は1以上で入力してください")
    private Integer quantity;
}
