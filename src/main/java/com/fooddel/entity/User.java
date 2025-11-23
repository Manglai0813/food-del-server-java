package com.fooddel.entity;

import com.fooddel.constant.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * ユーザーエンティティ
 * PrismaスキーマからJPAエンティティへの変換
 * Spring SecurityのUserDetailsを実装します。
 */
@Entity
@Table(name = "users")
@Data
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {

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

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    // --- UserDetailsの実装 ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        // Spring Securityでは、usernameは一意の識別子を指します。この場合はメールアドレスです。
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // アカウントの有効期限切れロジックは未実装
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // アカウントのロックロジックは未実装
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 資格情報の有効期限切れロジックは未実装
    }

    @Override
    public boolean isEnabled() {
        return true; // アカウントの有効/無効ロジックは未実装
    }


    // --- リレーションシップ ---

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
