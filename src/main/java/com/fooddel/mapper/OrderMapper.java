package com.fooddel.mapper;

import com.fooddel.dto.response.OrderResponse;
import com.fooddel.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * OrderエンティティとDTO間のマッピングインターフェース
 */
@Mapper(componentModel = "spring", uses = {UserMapper.class, OrderItemMapper.class})
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    // OrderエンティティからOrderResponseへマッピング
    OrderResponse toResponse(Order order);
}
