package com.fooddel.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * カテゴリ情報レスポンス用のDTO
 */
@Data
public class CategoryResponse {
    private Integer id;
    private String name;
    private String description;
    private Boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
