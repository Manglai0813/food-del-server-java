package com.fooddel.mapper;

import com.fooddel.dto.request.OrderRequest;
import com.fooddel.dto.response.OrderItemResponse;
import com.fooddel.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * OrderItemエンティティとDTO間のマッピングインターフェース
 */
@Mapper(componentModel = "spring", uses = {FoodMapper.class})
public interface OrderItemMapper {

    OrderItemMapper INSTANCE = Mappers.getMapper(OrderItemMapper.class);

    // OrderRequest.OrderItemRequestからOrderItemエンティティへマッピング
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "order", ignore = true) // 注文は別途設定
    @Mapping(target = "food", ignore = true) // 商品は別途設定
    @Mapping(target = "price", ignore = true) // 価格は別途設定
    OrderItem toEntity(OrderRequest.OrderItemRequest orderItemRequest);

    // OrderItemエンティティからOrderItemResponseへマッピング
    OrderItemResponse toResponse(OrderItem orderItem);
}
