package com.fooddel.mapper;

import com.fooddel.dto.response.CartResponse;
import com.fooddel.entity.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * CartエンティティとDTO間のマッピングインターフェース
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, CartItemMapper.class})
public interface CartMapper {

    CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

    // CartエンティティからCartResponseへマッピング
    CartResponse toResponse(Cart cart);
}
