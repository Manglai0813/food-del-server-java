package com.fooddel.mapper;

import com.fooddel.dto.request.CartItemRequest;
import com.fooddel.dto.response.CartItemResponse;
import com.fooddel.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * CartItemエンティティとDTO間のマッピングインターフェース
 */
@Mapper(componentModel = "spring", uses = {FoodMapper.class})
public interface CartItemMapper {

    CartItemMapper INSTANCE = Mappers.getMapper(CartItemMapper.class);

    // CartItemRequestからCartItemエンティティへマッピング
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "cart", ignore = true) // カートは別途設定
    @Mapping(target = "food", ignore = true) // 商品は別途設定
    CartItem toEntity(CartItemRequest cartItemRequest);

    // CartItemエンティティからCartItemResponseへマッピング
    CartItemResponse toResponse(CartItem cartItem);
}
