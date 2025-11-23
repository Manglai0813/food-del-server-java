package com.fooddel.mapper;

import com.fooddel.dto.request.FoodRequest;
import com.fooddel.dto.response.FoodResponse;
import com.fooddel.entity.Food;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * FoodエンティティとDTO間のマッピングインターフェース
 */
@Mapper(componentModel = "spring", uses = { CategoryMapper.class }) // CategoryMapperを使用
public interface FoodMapper {

    FoodMapper INSTANCE = Mappers.getMapper(FoodMapper.class);

    // FoodRequestからFoodエンティティへマッピング
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true) // カテゴリは別途設定
    @Mapping(target = "cartItems", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "inventoryHistory", ignore = true)
    @Mapping(target = "imagePath", ignore = true)
    @Mapping(target = "stock", ignore = true)
    @Mapping(target = "reserved", ignore = true)
    @Mapping(target = "minStock", ignore = true)
    @Mapping(target = "version", ignore = true)
    Food toEntity(FoodRequest foodRequest);

    // FoodエンティティからFoodResponseへマッピング
    FoodResponse toResponse(Food food);

    // FoodRequestから既存のFoodエンティティへ更新
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "category", ignore = true) // カテゴリは別途設定
    @Mapping(target = "cartItems", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "inventoryHistory", ignore = true)
    @Mapping(target = "imagePath", ignore = true)
    @Mapping(target = "stock", ignore = true)
    @Mapping(target = "reserved", ignore = true)
    @Mapping(target = "minStock", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromRequest(FoodRequest foodRequest, @MappingTarget Food food);
}
