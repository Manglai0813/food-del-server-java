package com.fooddel.controller;

import com.fooddel.dto.request.CategoryRequest;
import com.fooddel.dto.response.ApiResponse;
import com.fooddel.dto.response.CategoryResponse;
import com.fooddel.dto.response.PaginatedResponse;
import com.fooddel.entity.Category;
import com.fooddel.exception.custom.BusinessException;
import com.fooddel.mapper.CategoryMapper;
import com.fooddel.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * カテゴリ関連のAPIエンドポイントを処理するコントローラー
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    public CategoryController(CategoryService categoryService, CategoryMapper categoryMapper) {
        this.categoryService = categoryService;
        this.categoryMapper = categoryMapper;
    }

    /**
     * 新しいカテゴリを作成します。
     * @param categoryRequest カテゴリ作成リクエスト
     * @return 作成されたカテゴリ情報
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        Category createdCategory = categoryService.createCategory(categoryRequest);
        CategoryResponse categoryResponse = categoryMapper.toResponse(createdCategory);
        ApiResponse<CategoryResponse> response = ApiResponse.success(categoryResponse, "カテゴリが正常に作成されました。");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * 全てのカテゴリをページネーション付きで取得します。
     * @param pageable ページネーション情報
     * @return カテゴリのページ
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PaginatedResponse<CategoryResponse>>> getAllCategories(Pageable pageable) {
        Page<Category> categoryPage = categoryService.findAllCategories(pageable);
        List<CategoryResponse> categoryResponses = categoryPage.getContent().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());

        PaginatedResponse<CategoryResponse> paginatedData = new PaginatedResponse<>(
                categoryResponses,
                new com.fooddel.dto.response.PaginationInfo(
                        categoryPage.getNumber(),
                        categoryPage.getSize(),
                        categoryPage.getTotalElements(),
                        categoryPage.getTotalPages(),
                        categoryPage.hasNext(),
                        categoryPage.hasPrevious()
                )
        );

        ApiResponse<PaginatedResponse<CategoryResponse>> response = ApiResponse.success(paginatedData, "カテゴリリストが正常に取得されました。");
        return ResponseEntity.ok(response);
    }

    /**
     * 指定されたIDのカテゴリを取得します。
     * @param id カテゴリID
     * @return カテゴリ情報
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Integer id) {
        return categoryService.findCategoryById(id)
                .map(categoryMapper::toResponse)
                .map(categoryResponse -> ApiResponse.success(categoryResponse, "カテゴリが正常に取得されました。"))
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "指定されたカテゴリが見つかりません。"));
    }

    /**
     * 指定されたIDのカテゴリを更新します。
     * @param id カテゴリID
     * @param categoryRequest カテゴリ更新リクエスト
     * @return 更新されたカテゴリ情報
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(@PathVariable Integer id, @Valid @RequestBody CategoryRequest categoryRequest) {
        Category updatedCategory = categoryService.updateCategory(id, categoryRequest);
        CategoryResponse categoryResponse = categoryMapper.toResponse(updatedCategory);
        ApiResponse<CategoryResponse> response = ApiResponse.success(categoryResponse, "カテゴリが正常に更新されました。");
        return ResponseEntity.ok(response);
    }

    /**
     * 指定されたIDのカテゴリを削除します。
     * @param id カテゴリID
     * @return 削除成功レスポンス
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        ApiResponse<Void> response = ApiResponse.success("カテゴリが正常に削除されました。");
        return ResponseEntity.ok(response);
    }
}
