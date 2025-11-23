package com.fooddel.service;

import com.fooddel.dto.request.CategoryRequest;
import com.fooddel.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * カテゴリ関連のビジネスロジックを定義するサービスインターフェース
 */
public interface CategoryService {

    // 新しいカテゴリを作成します。
    Category createCategory(CategoryRequest categoryRequest);

    // 指定されたIDのカテゴリを検索します。
    Optional<Category> findCategoryById(Integer id);

    // 全てのカテゴリをページネーション付きで取得します。
    Page<Category> findAllCategories(Pageable pageable);

    // 指定されたステータスのカテゴリをすべて取得します。
    List<Category> findCategoriesByStatus(Boolean status);

    // 指定されたIDのカテゴリを更新します。
    Category updateCategory(Integer id, CategoryRequest categoryRequest);

    // 指定されたIDのカテゴリを削除します。
    void deleteCategory(Integer id);
}
