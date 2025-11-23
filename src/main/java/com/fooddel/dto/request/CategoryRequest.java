package com.fooddel.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * カテゴリ作成・更新リクエスト用のDTO
 */
@Data
public class CategoryRequest {

    @NotBlank(message = "カテゴリ名は必須です")
    @Size(max = 255, message = "カテゴリ名は255文字以内で入力してください")
    private String name;

    private String description;

    private Boolean status;
}
