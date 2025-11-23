package com.fooddel.mapper;

import com.fooddel.dto.request.CategoryRequest;
import com.fooddel.dto.response.CategoryResponse;
import com.fooddel.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * CategoryエンティティとDTO間のマッピングインターフェース
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    // CategoryRequestからCategoryエンティティへマッピング
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "foods", ignore = true) // foodsは別途設定
    Category toEntity(CategoryRequest categoryRequest);

    // CategoryエンティティからCategoryResponseへマッピング
    CategoryResponse toResponse(Category category);

    // CategoryRequestから既存のCategoryエンティティへ更新
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "foods", ignore = true) // foodsは別途設定
    void updateEntityFromRequest(CategoryRequest categoryRequest, @MappingTarget Category category);
}
