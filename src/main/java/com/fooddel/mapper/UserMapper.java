package com.fooddel.mapper;

import com.fooddel.dto.request.UserRequest;
import com.fooddel.dto.response.UserResponse;
import com.fooddel.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

/**
 * UserエンティティとDTO間のマッピングインターフェース
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // UserRequestからUserエンティティへマッピング
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    // authorities is derived from role, so we ignore it in mapping
    User toEntity(UserRequest userRequest);

    // UserエンティティからUserResponseへマッピング
    UserResponse toResponse(User user);

    // UserRequestから既存のUserエンティティへ更新
    @Mapping(target = "id", ignore = true) // IDは更新しない
    @Mapping(target = "createdAt", ignore = true) // 作成日時は更新しない
    @Mapping(target = "updatedAt", ignore = true) // 更新日時はJPA Auditingで自動更新
    @Mapping(target = "password", ignore = true) // パスワードは別途更新
    @Mapping(target = "role", ignore = true) // ロールは別途更新
    @Mapping(target = "refreshToken", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    void updateEntityFromRequest(UserRequest userRequest, @MappingTarget User user);
}
