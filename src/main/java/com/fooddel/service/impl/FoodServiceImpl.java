package com.fooddel.service.impl;

import com.fooddel.dto.request.FoodRequest;
import com.fooddel.entity.Category;
import com.fooddel.entity.Food;
import com.fooddel.exception.custom.BusinessException;
import com.fooddel.mapper.FoodMapper;
import com.fooddel.repository.CategoryRepository;
import com.fooddel.repository.FoodRepository;
import com.fooddel.service.FoodService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 商品関連のビジネスロジックを実装するサービス
 */
@Service
public class FoodServiceImpl implements FoodService {

    private final FoodRepository foodRepository;
    private final CategoryRepository categoryRepository;
    private final FoodMapper foodMapper;

    public FoodServiceImpl(FoodRepository foodRepository, CategoryRepository categoryRepository,
            FoodMapper foodMapper) {
        this.foodRepository = foodRepository;
        this.categoryRepository = categoryRepository;
        this.foodMapper = foodMapper;
    }

    // 新しい商品を作成します。
    @Override
    @Transactional
    public Food createFood(FoodRequest foodRequest) {
        // カテゴリの存在チェック
        Category category = categoryRepository.findById(foodRequest.getCategoryId())
                .orElseThrow(
                        () -> new BusinessException(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "指定されたカテゴリが見つかりません。"));

        // DTOからエンティティへの変換
        Food food = foodMapper.toEntity(foodRequest);
        food.setCategory(category); // カテゴリを設定

        return foodRepository.save(food);
    }

    // 指定されたIDの商品を検索します。
    @Override
    @Transactional(readOnly = true)
    public Optional<Food> findFoodById(Integer id) {
        return foodRepository.findById(id);
    }

    // 全ての商品をページネーション付きで取得します（管理者用など）。
    @Override
    @Transactional(readOnly = true)
    public Page<Food> findAllFoods(Pageable pageable) {
        return foodRepository.findAll(pageable);
    }

    // 公開されている商品のみをページネーション付きで取得します。
    @Override
    @Transactional(readOnly = true)
    public Page<Food> findPublicFoods(Pageable pageable) {
        return foodRepository.findByStatus(true, pageable);
    }

    // 指定されたカテゴリIDに属する商品をページネーション付きで取得します。
    @Override
    @Transactional(readOnly = true)
    public Page<Food> findFoodsByCategory(Integer categoryId, Pageable pageable) {
        return foodRepository.findByCategoryIdAndStatus(categoryId, true, pageable);
    }

    // 注目商品を指定された数だけ取得します。
    @Override
    @Transactional(readOnly = true)
    public List<Food> findFeaturedFoods(int limit) {
        // 最新の商品を注目商品として返す
        return foodRepository
                .findAll(
                        PageRequest.of(0, limit,
                                org.springframework.data.domain.Sort
                                        .by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")))
                .getContent();
    }

    // 指定されたIDの商品を更新します。
    @Override
    @Transactional
    public Food updateFood(Integer id, FoodRequest foodRequest) {
        Food existingFood = foodRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "FOOD_NOT_FOUND", "指定された商品が見つかりません。"));

        // マッパーを使用してエンティティを更新
        foodMapper.updateEntityFromRequest(foodRequest, existingFood);

        // カテゴリが変更された場合、新しいカテゴリを設定
        Optional.ofNullable(foodRequest.getCategoryId())
                .filter(newCategoryId -> !newCategoryId.equals(existingFood.getCategory().getId()))
                .ifPresent(newCategoryId -> {
                    Category category = categoryRepository.findById(newCategoryId)
                            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND",
                                    "指定されたカテゴリが見つかりません。"));
                    existingFood.setCategory(category);
                });

        return foodRepository.save(existingFood);
    }

    // 指定されたIDの商品を削除します。
    @Override
    @Transactional
    public void deleteFood(Integer id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "FOOD_NOT_FOUND", "指定された商品が見つかりません。"));

        food.setStatus(false); // Soft delete
        foodRepository.save(food);
    }

    // 動的な条件で商品を検索します。
    @Override
    @Transactional(readOnly = true)
    public Page<Food> findFoods(Specification<Food> spec, Pageable pageable) {
        return foodRepository.findAll(spec, pageable);
    }
}
