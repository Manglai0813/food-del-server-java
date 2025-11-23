package com.fooddel.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal; // BigDecimalをインポート
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品エンティティ
 * PrismaスキーマからJPAエンティティへの変換
 */
@Entity
@Table(name = "foods")
@Data
@EntityListeners(AuditingEntityListener.class)
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Column(nullable = false)
    private Boolean status = true; // デフォルト値

    @Column(nullable = false)
    private Integer stock = 0; // デフォルト値

    @Column(nullable = false)
    private Integer reserved = 0; // デフォルト値

    @Column(name = "min_stock", nullable = false)
    private Integer minStock = 0; // デフォルト値

    @Version // 楽観的ロック
    private Integer version = 1; // デフォルト値

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // リレーションシップ
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false) // category_id は必須
    private Category category;

    @OneToMany(mappedBy = "food", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "food", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "food", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InventoryHistory> inventoryHistory;
}
