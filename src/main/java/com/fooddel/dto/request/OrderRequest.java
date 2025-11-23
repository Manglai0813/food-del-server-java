package com.fooddel.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 注文作成リクエスト用のDTO
 */
@Data
public class OrderRequest {

    @NotBlank(message = "配送先住所は必須です")
    @Size(max = 500, message = "配送先住所は500文字以内で入力してください")
    private String deliveryAddress;

    @NotNull(message = "注文アイテムは必須です")
    @Size(min = 1, message = "注文アイテムは最低1つ必要です")
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        @NotNull(message = "商品IDは必須です")
        private Integer foodId;

        @NotNull(message = "数量は必須です")
        @Min(value = 1, message = "数量は1以上で入力してください")
        private Integer quantity;
    }
}
