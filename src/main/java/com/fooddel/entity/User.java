package com.fooddel.entity;

import com.fooddel.constant.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ユーザーエンティティ
 * PrismaスキーマからJPAエンティティへの変換
 */
@Entity
@Table(name = "users")
@Data
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING) // 列挙型を文字列としてDBに保存
    @Column(nullable = false)
    private UserRole role = UserRole.CUSTOMER; // デフォルト値

    @Column(nullable = true)
    private String phone;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // リレーションシップ (関連エンティティが未作成のため、mappedByのみ定義)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Cart cart;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders;

    // TODO: OrderStatusHistory と InventoryHistory のリレーションは、
    // 関連エンティティの設計に応じて調整が必要
    // @OneToMany(mappedBy = "updatedByUser")
    // private List<OrderStatusHistory> statusUpdates;

    // @OneToMany(mappedBy = "createdByUser")
    // private List<InventoryHistory> inventoryChanges;
}
