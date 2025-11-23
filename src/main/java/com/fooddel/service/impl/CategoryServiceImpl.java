package com.fooddel.service.impl;

import com.fooddel.dto.request.CategoryRequest;
import com.fooddel.entity.Category;
import com.fooddel.exception.custom.BusinessException;
import com.fooddel.mapper.CategoryMapper;
import com.fooddel.repository.CategoryRepository;
import com.fooddel.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * カテゴリ関連のビジネスロジックを実装するサービス
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final com.fooddel.repository.FoodRepository foodRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper,
            com.fooddel.repository.FoodRepository foodRepository) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.foodRepository = foodRepository;
    }

    // 新しいカテゴリを作成します。
    @Override
    @Transactional
    public Category createCategory(CategoryRequest categoryRequest) {
        // カテゴリ名の一意性チェック
        categoryRepository.findByName(categoryRequest.getName())
                .ifPresent(category -> {
                    throw new BusinessException(HttpStatus.CONFLICT, "CATEGORY_ALREADY_EXISTS", "指定されたカテゴリ名は既に存在します。");
                });

        // DTOからエンティティへの変換
        Category category = categoryMapper.toEntity(categoryRequest);
        // デフォルトステータスを設定
        if (category.getStatus() == null) {
            category.setStatus(true);
        }

        return categoryRepository.save(category);
    }

    // 指定されたIDのカテゴリを検索します。
    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findCategoryById(Integer id) {
        return categoryRepository.findById(id);
    }

    // 全てのカテゴリをページネーション付きで取得します。
    @Override
    @Transactional(readOnly = true)
    public Page<Category> findAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    // 指定されたステータスのカテゴリをすべて取得します。
    @Override
    @Transactional(readOnly = true)
    public List<Category> findCategoriesByStatus(Boolean status) {
        return categoryRepository.findByStatus(status);
    }

    // 指定されたIDのカテゴリを更新します。
    @Override
    @Transactional
    public Category updateCategory(Integer id, CategoryRequest categoryRequest) {
        // 更新対象のカテゴリを検索
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(
                        () -> new BusinessException(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "指定されたカテゴリが見つかりません。"));

        // カテゴリ名が変更された場合、一意性チェック
        Optional.ofNullable(categoryRequest.getName())
                .filter(newName -> !newName.equals(existingCategory.getName()))
                .ifPresent(newName -> categoryRepository.findByName(newName)
                        .ifPresent(category -> {
                            throw new BusinessException(HttpStatus.CONFLICT, "CATEGORY_ALREADY_EXISTS",
                                    "指定されたカテゴリ名は既に存在します。");
                        }));

        // マッパーを使用してエンティティを更新
        categoryMapper.updateEntityFromRequest(categoryRequest, existingCategory);

        return categoryRepository.save(existingCategory);
    }

    // 指定されたIDのカテゴリを削除します。
    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        // 商品が存在するカテゴリは削除できないようにする
        if (foodRepository.existsByCategoryId(id)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "CANNOT_DELETE_CATEGORY_WITH_FOODS",
                    "商品が登録されているカテゴリは削除できません。");
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(
                        () -> new BusinessException(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "指定されたカテゴリが見つかりません。"));

        categoryRepository.delete(category);
    }
}
