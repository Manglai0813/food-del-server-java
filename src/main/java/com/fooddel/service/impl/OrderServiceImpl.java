package com.fooddel.service.impl;

import com.fooddel.constant.enums.OrderStatus;
import com.fooddel.dto.request.OrderRequest;
import com.fooddel.entity.*;
import com.fooddel.exception.custom.BusinessException;
import com.fooddel.repository.FoodRepository;
import com.fooddel.repository.OrderRepository;
import com.fooddel.repository.UserRepository;
import com.fooddel.service.CartService;
import com.fooddel.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal; // BigDecimalをインポート
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 注文関連のビジネスロジックを実装するサービス
 */
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final FoodRepository foodRepository;
    private final CartService cartService;

    public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository,
            FoodRepository foodRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.foodRepository = foodRepository;
        this.cartService = cartService;
    }

    // 新しい注文を作成します。
    @Override
    @Transactional
    public Order createOrder(Integer userId, OrderRequest orderRequest) {
        // 1. ユーザーとカートを取得
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "ユーザーが見つかりません。"));
        Cart cart = cartService.getCartByUserId(userId);
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "CART_EMPTY", "カートが空です。");
        }

        // 2. 在庫のロックと検証、および合計金額の計算
        BigDecimal totalAmount = BigDecimal.ZERO; // doubleからBigDecimalに変更
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getCartItems()) {
            // 悲観的ロックで商品を取得
            Food food = foodRepository.findByIdWithLock(cartItem.getFood().getId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "FOOD_NOT_FOUND",
                            "商品が見つかりません: " + cartItem.getFood().getName()));

            // 在庫チェック
            if (food.getStock() < cartItem.getQuantity()) {
                throw new BusinessException(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", "在庫不足です: " + food.getName());
            }

            // 注文アイテムを作成
            OrderItem orderItem = new OrderItem();
            orderItem.setFood(food);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(food.getPrice()); // 注文時の価格を記録
            orderItems.add(orderItem);

            // 合計金額を計算 (BigDecimalの演算を使用)
            totalAmount = totalAmount.add(food.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            // 在庫を更新（実在庫から減算）
            food.setStock(food.getStock() - cartItem.getQuantity());
            // 予約を解除（カートに入れた時に予約していた分を解除）
            food.setReserved(food.getReserved() - cartItem.getQuantity());
            foodRepository.save(food); // 明示的に保存
        }

        // 3. 注文エンティティを作成
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(totalAmount); // BigDecimalをセット
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress(orderRequest.getDeliveryAddress());
        order.setOrderItems(orderItems);

        // 注文アイテムに注文をセット
        orderItems.forEach(item -> item.setOrder(order));

        // 4. 注文を保存
        Order savedOrder = orderRepository.save(order);

        // 5. カートを空にする
        cartService.clearCart(userId);

        return savedOrder;
    }

    // 指定されたIDの注文を検索します。
    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findOrderById(Integer orderId) {
        return orderRepository.findById(orderId);
    }

    // 指定されたユーザーの注文をページネーション付きで取得します。
    @Override
    @Transactional(readOnly = true)
    public Page<Order> findOrdersByUserId(Integer userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    // 注文をキャンセルします。
    @Override
    @Transactional
    public Order cancelOrder(Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "ORDER_NOT_FOUND", "指定された注文が見つかりません。"));

        // キャンセル可能なステータスかチェック
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "ORDER_CANNOT_BE_CANCELLED", "この注文はキャンセルできません。");
        }

        // 在庫を戻す
        for (OrderItem item : order.getOrderItems()) {
            Food food = foodRepository.findByIdWithLock(item.getFood().getId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "FOOD_NOT_FOUND_ON_CANCEL", "注文キャンセル中に商品が見つかりませんでした。"));
            food.setStock(food.getStock() + item.getQuantity());
            foodRepository.save(food);
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    // 全ての注文をページネーション付きで取得します（管理者用）。
    @Override
    @Transactional(readOnly = true)
    public Page<Order> findAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
}
