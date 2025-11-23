package com.fooddel.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商品作成・更新リクエスト用のDTO
 */
@Data
public class FoodRequest {

    @NotBlank(message = "商品名は必須です")
    @Size(max = 255, message = "商品名は255文字以内で入力してください")
    private String name;

    @NotBlank(message = "商品説明は必須です")
    private String description;

    @NotNull(message = "価格は必須です")
    @Min(value = 0, message = "価格は0以上で入力してください")
    private Double price;

    @NotNull(message = "カテゴリIDは必須です")
    private Integer categoryId;

    private Boolean status;
}
