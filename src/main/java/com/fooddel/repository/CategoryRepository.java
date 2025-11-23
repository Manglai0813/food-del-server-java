package com.fooddel.repository;

import com.fooddel.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * カテゴリエンティティ用のリポジトリインターフェース
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    /**
     * カテゴリ名でカテゴリを検索します。
     * @param name 検索するカテゴリ名
     * @return カテゴリ（オプショナル）
     */
    Optional<Category> findByName(String name);

    /**
     * 指定されたステータスのカテゴリをすべて検索します。
     * @param status 検索するステータス
     * @return カテゴリのリスト
     */
    List<Category> findByStatus(Boolean status);
}
