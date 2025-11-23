package com.fooddel.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品情報レスポンス用のDTO
 */
@Data
public class FoodResponse {
    private Integer id;
    private String name;
    private String description;
    private Double price;
    private String imagePath;
    private Boolean status;
    private Integer stock;
    private Integer reserved;
    private Integer minStock;
    private Integer version; // 楽観的ロック用
    private CategoryResponse category; // カテゴリ情報
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
