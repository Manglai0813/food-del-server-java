# データベース設計 - Prisma から JPA への変換

## 概要

既存の Prisma スキーマを JPA エンティティに変換する際の設計指針と対応関係を説明するドキュメントです。

## Prisma と JPA の対応関係

### 基本型の対応

| Prisma | JPA | 説明 |
|--------|-----|------|
| `Int` | `Integer` または `Long` | 整数型 |
| `String` | `String` | 文字列型 |
| `Float` | `Double` | 浮動小数点型 |
| `Boolean` | `Boolean` | 真偽値型 |
| `DateTime` | `LocalDateTime` | 日時型 |
| `@id @default(autoincrement())` | `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)` | 主キー |
| `@unique` | `@Column(unique = true)` | ユニーク制約 |
| `@default(...)` | `@ColumnDefault(...)` またはデフォルト値 | デフォルト値 |
| `@map("table_name")` | `@Table(name = "table_name")` | テーブル名マッピング |

### リレーションシップの対応

| Prisma | JPA | 説明 |
|--------|-----|------|
| `@relation(fields: [...], references: [...])` | `@ManyToOne`, `@OneToMany`, `@OneToOne` | リレーション |
| `?` (オプショナル) | `@ManyToOne(optional = true)` | オプショナルリレーション |

---

## エンティティ変換詳細

### 1. User エンティティ

#### Prisma スキーマ
```prisma
model User {
  id         Int      @id @default(autoincrement())
  name       String
  email      String   @unique
  password   String
  role       String   @default("customer")
  phone      String?
  created_at DateTime @default(now())
  updated_at DateTime @updatedAt
  
  cart               Cart?
  orders             Order[]
  status_updates     OrderStatusHistory[]
  inventory_changes  InventoryHistory[]
  
  @@map("users")
}
```

#### JPA エンティティ設計

**主要フィールド**:
- `id`: `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`
- `email`: `@Column(unique = true, nullable = false)`
- `role`: `@Column(nullable = false) @Enumerated(EnumType.STRING)` または `String`
- `phone`: `@Column(nullable = true)`
- `createdAt`: `@CreatedDate` (JPA Auditing)
- `updatedAt`: `@LastModifiedDate` (JPA Auditing)

**リレーション**:
- `cart`: `@OneToOne(mappedBy = "user", cascade = CascadeType.ALL)`
- `orders`: `@OneToMany(mappedBy = "user")`
- `statusUpdates`: `@OneToMany(mappedBy = "updatedByUser")`
- `inventoryChanges`: `@OneToMany(mappedBy = "createdByUser")`

**注意点**:
- `password` フィールドはレスポンスに含めない
- `role` は列挙型 `UserRole` として定義することを推奨

---

### 2. Food エンティティ

#### Prisma スキーマ
```prisma
model Food {
  id          Int      @id @default(autoincrement())
  name        String
  description String
  price       Float
  category_id Int
  image_path  String
  status      Boolean  @default(true)
  
  stock       Int      @default(0)
  reserved    Int      @default(0)
  min_stock   Int      @default(0)
  version     Int      @default(1)
  
  created_at  DateTime @default(now())
  updated_at  DateTime @updatedAt
  
  category         Category           @relation(...)
  cart_items       CartItem[]
  order_items      OrderItem[]
  inventory_history InventoryHistory[]
  
  @@map("foods")
}
```

#### JPA エンティティ設計

**主要フィールド**:
- `id`: `@Id @GeneratedValue`
- `name`: `@Column(nullable = false)`
- `description`: `@Column(nullable = false, columnDefinition = "TEXT")`
- `price`: `@Column(nullable = false, precision = 10, scale = 2)`
- `imagePath`: `@Column(nullable = false)`
- `status`: `@Column(nullable = false)`
- `stock`: `@Column(nullable = false)`
- `reserved`: `@Column(nullable = false)`
- `minStock`: `@Column(nullable = false)`
- `version`: `@Version` (楽観的ロック)
- `createdAt`: `@CreatedDate`
- `updatedAt`: `@LastModifiedDate`

**リレーション**:
- `category`: `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id")`
- `cartItems`: `@OneToMany(mappedBy = "food")`
- `orderItems`: `@OneToMany(mappedBy = "food")`
- `inventoryHistory`: `@OneToMany(mappedBy = "food")`

**重要ポイント**:
- `version` フィールドは `@Version` アノテーションを使用（楽観的ロック）
- `category` は `LAZY` フェッチを推奨（必要時のみ取得）

---

## 命名規則の変換

### フィールド名

**Prisma (snake_case)** → **JPA (camelCase)**:
- `user_id` → `userId`
- `created_at` → `createdAt`
- `updated_at` → `updatedAt`
- `category_id` → `categoryId`
- `order_id` → `orderId`
- `food_id` → `foodId`
- `image_path` → `imagePath`
- `min_stock` → `minStock`
- `total_amount` → `totalAmount`
- `delivery_address` → `deliveryAddress`
- `previous_status` → `previousStatus`
- `new_status` → `newStatus`
- `updated_by` → `updatedBy`
- `change_type` → `changeType`
- `previous_stock` → `previousStock`
- `new_stock` → `newStock`
- `created_by` → `createdBy`

### テーブル名

**Prisma** → **JPA**:
- `users` → `@Table(name = "users")`
- `categories` → `@Table(name = "categories")`
- `foods` → `@Table(name = "foods")`
- `carts` → `@Table(name = "carts")`
- `cart_items` → `@Table(name = "cart_items")`
- `orders` → `@Table(name = "orders")`
- `order_items` → `@Table(name = "order_items")`
- `order_status_history` → `@Table(name = "order_status_history")`
- `inventory_history` → `@Table(name = "inventory_history")`

---

## 列挙型の定義

### UserRole

```java
public enum UserRole {
    CUSTOMER,
    STAFF,
    ADMIN
}
```

### OrderStatus

```java
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    DELIVERY,
    COMPLETED,
    CANCELLED
}
```

### InventoryChangeType

```java
public enum InventoryChangeType {
    ADD,
    SUBTRACT,
    RESERVE,
    RELEASE
}
```

---

## 楽観的ロック

### Food エンティティ

**Prisma**:
```prisma
version     Int      @default(1)
```

**JPA**:
```java
@Version
private Integer version;
```

**使用方法**:
- 在庫更新時に自動的にバージョンチェック
- 競合時は `OptimisticLockException` がスローされる

---

## 監査機能（JPA Auditing）

### 実装方法

**設定**:
- `@EnableJpaAuditing` を設定クラスに追加
- エンティティに `@EntityListeners(AuditingEntityListener.class)` を追加

**フィールド**:
- `@CreatedDate`: 作成日時（`created_at`）
- `@LastModifiedDate`: 更新日時（`updated_at`）

**対象エンティティ**:
- User, Category, Food, Cart, Order

---

## インデックス設計

### 既存のインデックス（Prisma から）

**OrderStatusHistory**:
- `order_id`: 注文IDでの検索
- `updated_at`: 日付ソート

**InventoryHistory**:
- `food_id`: 商品IDでの検索
- `created_at`: 日付ソート
- `change_type`: 変更タイプでのフィルタ

### 追加推奨インデックス

**Food**:
- `category_id`: カテゴリ検索
- `status`: ステータスフィルタ

**Order**:
- `user_id`: ユーザー別注文検索
- `order_date`: 日付ソート
- `status`: ステータスフィルタ

**CartItem**:
- `cart_id`: カートアイテム取得

---

**作成日**: 2025-01-XX  
**バージョン**: 1.0

