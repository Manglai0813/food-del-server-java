package com.fooddel.specification;

import com.fooddel.entity.Food;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 商品の動的な検索条件を構築するためのSpecificationクラス
 */
public class FoodSpecification {

    public static Specification<Food> withDynamicQuery(
            String name,
            Double minPrice,
            Double maxPrice,
            Integer categoryId,
            Boolean status
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            Optional.ofNullable(name)
                    .filter(s -> !s.isEmpty())
                    .ifPresent(s -> predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + s.toLowerCase() + "%")));

            Optional.ofNullable(minPrice)
                    .ifPresent(price -> predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), price)));

            Optional.ofNullable(maxPrice)
                    .ifPresent(price -> predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), price)));

            Optional.ofNullable(categoryId)
                    .ifPresent(id -> predicates.add(criteriaBuilder.equal(root.get("category").get("id"), id)));

            Optional.ofNullable(status)
                    .ifPresent(s -> predicates.add(criteriaBuilder.equal(root.get("status"), s)));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
