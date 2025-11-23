package com.fooddel.service;

import com.fooddel.dto.request.FoodRequest;
import com.fooddel.entity.Food;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

/**
 * 商品関連のビジネスロジックを定義するサービスインターフェース
 */
public interface FoodService {

    // 新しい商品を作成します。
    Food createFood(FoodRequest foodRequest);

    // 指定されたIDの商品を検索します。
    Optional<Food> findFoodById(Integer id);

    // 全ての商品をページネーション付きで取得します（管理者用など）。
    Page<Food> findAllFoods(Pageable pageable);

    // 公開されている商品のみをページネーション付きで取得します。
    Page<Food> findPublicFoods(Pageable pageable);

    // 指定されたカテゴリIDに属する商品をページネーション付きで取得します。
    Page<Food> findFoodsByCategory(Integer categoryId, Pageable pageable);

    // 注目商品を指定された数だけ取得します。
    List<Food> findFeaturedFoods(int limit);

    // 指定されたIDの商品を更新します。
    Food updateFood(Integer id, FoodRequest foodRequest);

    // 指定されたIDの商品を削除します。
    void deleteFood(Integer id);

    // 動的な条件で商品を検索します。
    Page<Food> findFoods(Specification<Food> spec, Pageable pageable);
}
