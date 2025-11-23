package com.fooddel.controller;

import com.fooddel.dto.request.CartItemRequest;
import com.fooddel.dto.response.ApiResponse;
import com.fooddel.dto.response.CartResponse;
import com.fooddel.entity.Cart;
import com.fooddel.entity.User;
import com.fooddel.mapper.CartMapper;
import com.fooddel.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * カート関連のAPIエンドポイントを処理するコントローラー
 */
@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;
    private final CartMapper cartMapper;

    public CartController(CartService cartService, CartMapper cartMapper) {
        this.cartService = cartService;
        this.cartMapper = cartMapper;
    }

    /**
     * 認証済みユーザーのカート情報を取得します。
     * @param user 認証済みユーザー
     * @return カート情報
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@AuthenticationPrincipal User user) {
        Cart cart = cartService.getCartByUserId(user.getId());
        CartResponse cartResponse = cartMapper.toResponse(cart);
        ApiResponse<CartResponse> response = ApiResponse.success(cartResponse, "カート情報を取得しました。");
        return ResponseEntity.ok(response);
    }

    /**
     * カートに商品を追加します。
     * @param user 認証済みユーザー
     * @param cartItemRequest 追加する商品情報
     * @return 更新されたカート情報
     */
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItemToCart(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CartItemRequest cartItemRequest
    ) {
        Cart updatedCart = cartService.addItemToCart(user.getId(), cartItemRequest);
        CartResponse cartResponse = cartMapper.toResponse(updatedCart);
        ApiResponse<CartResponse> response = ApiResponse.success(cartResponse, "商品をカートに追加しました。");
        return ResponseEntity.ok(response);
    }

    /**
     * カート内の商品の数量を更新します。
     * @param user 認証済みユーザー
     * @param id カートアイテムID
     * @param cartItemRequest 更新する商品情報
     * @return 更新されたカート情報
     */
    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItemInCart(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Integer cartItemId,
            @Valid @RequestBody CartItemRequest cartItemRequest
    ) {
        Cart updatedCart = cartService.updateItemInCart(user.getId(), cartItemId, cartItemRequest);
        CartResponse cartResponse = cartMapper.toResponse(updatedCart);
        ApiResponse<CartResponse> response = ApiResponse.success(cartResponse, "カート内の商品を更新しました。");
        return ResponseEntity.ok(response);
    }

    /**
     * カートから商品を削除します。
     * @param user 認証済みユーザー
     * @param id カートアイテムID
     * @return 更新されたカート情報
     */
    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItemFromCart(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Integer cartItemId
    ) {
        Cart updatedCart = cartService.removeItemFromCart(user.getId(), cartItemId);
        CartResponse cartResponse = cartMapper.toResponse(updatedCart);
        ApiResponse<CartResponse> response = ApiResponse.success(cartResponse, "カートから商品を削除しました。");
        return ResponseEntity.ok(response);
    }

    /**
     * カートを空にします（予約も解除されます）。
     * @param user 認証済みユーザー
     * @return 成功レスポンス
     */
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal User user) {
        cartService.clearCartWithReservationRelease(user.getId());
        ApiResponse<Void> response = ApiResponse.success("カートを空にしました。");
        return ResponseEntity.ok(response);
    }
}
