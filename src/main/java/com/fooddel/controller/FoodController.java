package com.fooddel.controller;

import com.fooddel.dto.request.FoodRequest;
import com.fooddel.dto.response.ApiResponse;
import com.fooddel.dto.response.FoodResponse;
import com.fooddel.dto.response.PaginatedResponse;
import com.fooddel.entity.Food;
import com.fooddel.exception.custom.BusinessException;
import com.fooddel.mapper.FoodMapper;
import com.fooddel.service.FoodService;
import com.fooddel.specification.FoodSpecification;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品関連のAPIエンドポイントを処理するコントローラー
 */
@RestController
@RequestMapping("/api/foods")
public class FoodController {

    private final FoodService foodService;
    private final FoodMapper foodMapper;

    public FoodController(FoodService foodService, FoodMapper foodMapper) {
        this.foodService = foodService;
        this.foodMapper = foodMapper;
    }

    /**
     * 新しい商品を作成します。
     * @param foodRequest 商品作成リクエスト
     * @return 作成された商品情報
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FoodResponse>> createFood(@Valid @RequestBody FoodRequest foodRequest) {
        Food createdFood = foodService.createFood(foodRequest);
        FoodResponse foodResponse = foodMapper.toResponse(createdFood);
        ApiResponse<FoodResponse> response = ApiResponse.success(foodResponse, "商品が正常に作成されました。");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * 公開されている商品をページネーション付きで取得します。
     * @param name 商品名（部分一致）
     * @param minPrice 最小価格
     * @param maxPrice 最大価格
     * @param categoryId カテゴリID
     * @param pageable ページネーション情報
     * @return 商品のページ
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<FoodResponse>>> getPublicFoods(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer categoryId,
            Pageable pageable
    ) {
        Specification<Food> spec = FoodSpecification.withDynamicQuery(name, minPrice, maxPrice, categoryId, true);
        Page<Food> foodPage = foodService.findFoods(spec, pageable);
        return createPaginatedFoodResponse(foodPage, "公開商品リストが正常に取得されました。");
    }

    /**
     * 指定されたIDの商品を取得します。
     * @param id 商品ID
     * @return 商品情報
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FoodResponse>> getFoodById(@PathVariable Integer id) {
        return foodService.findFoodById(id)
                .map(foodMapper::toResponse)
                .map(foodResponse -> ApiResponse.success(foodResponse, "商品が正常に取得されました。"))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "FOOD_NOT_FOUND", "指定された商品が見つかりません。"));
    }

    /**
     * 指定されたIDの商品を更新します。
     * @param id 商品ID
     * @param foodRequest 商品更新リクエスト
     * @return 更新された商品情報
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FoodResponse>> updateFood(@PathVariable Integer id, @Valid @RequestBody FoodRequest foodRequest) {
        Food updatedFood = foodService.updateFood(id, foodRequest);
        FoodResponse foodResponse = foodMapper.toResponse(updatedFood);
        ApiResponse<FoodResponse> response = ApiResponse.success(foodResponse, "商品が正常に更新されました。");
        return ResponseEntity.ok(response);
    }

    /**
     * 指定されたIDの商品を削除します。
     * @param id 商品ID
     * @return 削除成功レスポンス
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFood(@PathVariable Integer id) {
        foodService.deleteFood(id);
        ApiResponse<Void> response = ApiResponse.success("商品が正常に削除されました。");
        return ResponseEntity.ok(response);
    }

    /**
     * 注目商品を取得します。
     * @param limit 取得する件数
     * @return 注目商品のリスト
     */
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<FoodResponse>>> getFeaturedFoods(@RequestParam(defaultValue = "5") int limit) {
        List<Food> featuredFoods = foodService.findFeaturedFoods(limit);
        List<FoodResponse> foodResponses = featuredFoods.stream()
                .map(foodMapper::toResponse)
                .collect(Collectors.toList());
        ApiResponse<List<FoodResponse>> response = ApiResponse.success(foodResponses, "注目商品が正常に取得されました。");
        return ResponseEntity.ok(response);
    }

    /**
     * 指定されたカテゴリIDの商品をページネーション付きで取得します。
     * @param id カテゴリID
     * @param pageable ページネーション情報
     * @return 商品のページ
     */
    @GetMapping("/category/{id}")
    public ResponseEntity<ApiResponse<PaginatedResponse<FoodResponse>>> getFoodsByCategory(@PathVariable Integer id, Pageable pageable) {
        Specification<Food> spec = FoodSpecification.withDynamicQuery(null, null, null, id, true);
        Page<Food> foodPage = foodService.findFoods(spec, pageable);
        return createPaginatedFoodResponse(foodPage, "カテゴリ別商品リストが正常に取得されました。");
    }

    // ページネーションレスポンスを生成するヘルパーメソッド
    private ResponseEntity<ApiResponse<PaginatedResponse<FoodResponse>>> createPaginatedFoodResponse(Page<Food> foodPage, String message) {
        List<FoodResponse> foodResponses = foodPage.getContent().stream()
                .map(foodMapper::toResponse)
                .collect(Collectors.toList());

        PaginatedResponse<FoodResponse> paginatedData = new PaginatedResponse<>(
                foodResponses,
                new com.fooddel.dto.response.PaginationInfo(
                        foodPage.getNumber(),
                        foodPage.getSize(),
                        foodPage.getTotalElements(),
                        foodPage.getTotalPages(),
                        foodPage.hasNext(),
                        foodPage.hasPrevious()
                )
        );

        ApiResponse<PaginatedResponse<FoodResponse>> response = ApiResponse.success(paginatedData, message);
        return ResponseEntity.ok(response);
    }
}
