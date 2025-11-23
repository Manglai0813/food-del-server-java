package com.fooddel.service.impl;

import com.fooddel.dto.request.CartItemRequest;
import com.fooddel.entity.Cart;
import com.fooddel.entity.CartItem;
import com.fooddel.entity.Food;
import com.fooddel.entity.User;
import com.fooddel.exception.custom.BusinessException;
import com.fooddel.repository.CartItemRepository;
import com.fooddel.repository.CartRepository;
import com.fooddel.repository.FoodRepository;
import com.fooddel.repository.UserRepository;
import com.fooddel.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

/**
 * カート関連のビジネスロジックを実装するサービス
 */
@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository,
            FoodRepository foodRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.foodRepository = foodRepository;
        this.userRepository = userRepository;
    }

    // 指定されたユーザーIDのカートを取得します。カートが存在しない場合は作成します。
    @Override
    @Transactional
    public Cart getCartByUserId(Integer userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
    }

    // カートに商品を追加します。
    @Override
    @Transactional
    public Cart addItemToCart(Integer userId, CartItemRequest cartItemRequest) {
        Cart cart = getCartByUserId(userId);
        Food food = foodRepository.findById(cartItemRequest.getFoodId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "FOOD_NOT_FOUND", "指定された商品が見つかりません。"));

        // 在庫チェック (予約分も考慮)
        if ((food.getStock() - food.getReserved()) < cartItemRequest.getQuantity()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INSUFFICIENT_STOCK", "在庫が不足しています。");
        }

        // カート内に同じ商品が既に存在するかチェック
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getFood().getId().equals(cartItemRequest.getFoodId()))
                .findFirst();

        existingItem.ifPresentOrElse(
                // 存在する場合：数量を更新
                item -> {
                    item.setQuantity(item.getQuantity() + cartItemRequest.getQuantity());
                    // 予約数を更新
                    food.setReserved(food.getReserved() + cartItemRequest.getQuantity());
                },
                // 存在しない場合：新しいアイテムを追加
                () -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setFood(food);
                    newItem.setQuantity(cartItemRequest.getQuantity());
                    cart.getCartItems().add(newItem);
                    // 予約数を更新
                    food.setReserved(food.getReserved() + cartItemRequest.getQuantity());
                });

        foodRepository.save(food); // 商品情報の更新（予約数）

        return cartRepository.save(cart);
    }

    // カート内の商品の数量を更新します。
    @Override
    @Transactional
    public Cart updateItemInCart(Integer userId, Integer cartItemId, CartItemRequest cartItemRequest) {
        Cart cart = getCartByUserId(userId);
        CartItem cartItem = findCartItemInCart(cart, cartItemId);

        int oldQuantity = cartItem.getQuantity();
        int newQuantity = cartItemRequest.getQuantity();
        int diff = newQuantity - oldQuantity;

        Food food = cartItem.getFood();

        // 増える場合のみ在庫チェック
        if (diff > 0) {
            if ((food.getStock() - food.getReserved()) < diff) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "INSUFFICIENT_STOCK", "在庫が不足しています。");
            }
        }

        // 予約数を更新
        food.setReserved(food.getReserved() + diff);
        foodRepository.save(food);

        cartItem.setQuantity(newQuantity);
        return cartRepository.save(cart);
    }

    // カートから商品を削除します。
    @Override
    @Transactional
    public Cart removeItemFromCart(Integer userId, Integer cartItemId) {
        Cart cart = getCartByUserId(userId);
        CartItem cartItem = findCartItemInCart(cart, cartItemId);

        Food food = cartItem.getFood();
        // 予約数を減らす
        food.setReserved(food.getReserved() - cartItem.getQuantity());
        foodRepository.save(food);

        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem); // CartItemを直接削除

        return cartRepository.save(cart);
    }

    // カートを空にします。
    @Override
    @Transactional
    public void clearCart(Integer userId) {
        Cart cart = getCartByUserId(userId);

        // カート内のアイテムをクリア
        // 注意：予約の解除は注文作成時に行われるため、ここでは不要
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }

    // 新しいカートを作成するプライベートメソッド
    private Cart createNewCart(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "指定されたユーザーが見つかりません。"));
        Cart newCart = new Cart();
        newCart.setUser(user);
        newCart.setCartItems(new ArrayList<>());
        return cartRepository.save(newCart);
    }

    // カート内の特定のカートアイテムを検索するヘルパーメソッド
    private CartItem findCartItemInCart(Cart cart, Integer cartItemId) {
        return cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "CART_ITEM_NOT_FOUND",
                        "カート内に指定された商品が見つかりません。"));
    }
}
