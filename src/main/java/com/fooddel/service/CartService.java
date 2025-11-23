package com.fooddel.service;

import com.fooddel.dto.request.CartItemRequest;
import com.fooddel.entity.Cart;

/**
 * カート関連のビジネスロジックを定義するサービスインターフェース
 */
public interface CartService {

    // 指定されたユーザーIDのカートを取得します。カートが存在しない場合は作成します。
    Cart getCartByUserId(Integer userId);

    // カートに商品を追加します。
    Cart addItemToCart(Integer userId, CartItemRequest cartItemRequest);

    // カート内の商品の数量を更新します。
    Cart updateItemInCart(Integer userId, Integer cartItemId, CartItemRequest cartItemRequest);

    // カートから商品を削除します。
    Cart removeItemFromCart(Integer userId, Integer cartItemId);

    // カートを空にします（予約は解除しません。注文作成時に使用）。
    void clearCart(Integer userId);

    // カートを空にし、予約も解除します（ユーザーが手動でカートをクリアする場合に使用）。
    void clearCartWithReservationRelease(Integer userId);
}
