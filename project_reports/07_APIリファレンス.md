# APIリファレンス

## 📋 目次

- [1. 認証API](#1-認証api)
- [2. 商品API](#2-商品api)
- [3. カートAPI](#3-カートapi)
- [4. 注文API](#4-注文api)
- [5. カテゴリAPI](#5-カテゴリapi)

---

## 1. 認証API

### 1.1 ユーザー登録

```
POST /api/auth/register
```

**リクエスト**:

```json
{
  "name": "山田太郎",
  "email": "yamada@example.com",
  "password": "password123"
}
```

**レスポンス** (201 Created):

```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "山田太郎",
    "email": "yamada@example.com",
    "role": "USER"
  },
  "message": "ユーザー登録が成功しました。"
}
```

### 1.2 ログイン

```
POST /api/auth/login
```

**リクエスト**:

```json
{
  "email": "yamada@example.com",
  "password": "password123"
}
```

**レスポンス** (200 OK):

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "user": {
      "id": 1,
      "name": "山田太郎",
      "email": "yamada@example.com",
      "role": "USER"
    }
  },
  "message": "ログインに成功しました。"
}
```

### 1.3 トークンリフレッシュ

```
POST /api/auth/refresh
```

**リクエスト**:

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**レスポンス** (200 OK):

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs..."
  },
  "message": "トークンが更新されました。"
}
```

### 1.4 ログアウト

```
POST /api/auth/logout
Authorization: Bearer {accessToken}
```

**レスポンス** (200 OK):

```json
{
  "success": true,
  "message": "ログアウトしました。"
}
```

---

## 2. 商品API

### 2.1 商品一覧取得

```
GET /api/foods?page=0&size=10
Authorization: Bearer {accessToken}
```

**レスポンス** (200 OK):

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "ハンバーガー",
        "description": "ジューシーなハンバーガー",
        "price": 500.00,
        "imagePath": "/uploads/burger.jpg",
        "stock": 50,
        "category": {
          "id": 1,
          "name": "メイン"
        }
      }
    ],
    "pagination": {
      "page": 0,
      "size": 10,
      "totalElements": 1,
      "totalPages": 1
    }
  }
}
```

### 2.2 商品詳細取得

```
GET /api/foods/{id}
Authorization: Bearer {accessToken}
```

### 2.3 商品作成（管理者のみ）

```
POST /api/foods
Authorization: Bearer {accessToken}
```

**リクエスト**:

```json
{
  "name": "ハンバーガー",
  "description": "ジューシーなハンバーガー",
  "price": 500.00,
  "stock": 50,
  "categoryId": 1
}
```

---

## 3. カートAPI

### 3.1 カート取得

```
GET /api/cart
Authorization: Bearer {accessToken}
```

**レスポンス** (200 OK):

```json
{
  "success": true,
  "data": {
    "id": 1,
    "cartItems": [
      {
        "id": 1,
        "food": {
          "id": 1,
          "name": "ハンバーガー",
          "price": 500.00
        },
        "quantity": 2
      }
    ]
  }
}
```

### 3.2 商品追加

```
POST /api/cart/items
Authorization: Bearer {accessToken}
```

**リクエスト**:

```json
{
  "foodId": 1,
  "quantity": 2
}
```

### 3.3 数量更新

```
PUT /api/cart/items/{cartItemId}
Authorization: Bearer {accessToken}
```

**リクエスト**:

```json
{
  "quantity": 3
}
```

### 3.4 商品削除

```
DELETE /api/cart/items/{cartItemId}
Authorization: Bearer {accessToken}
```

### 3.5 カートクリア

```
DELETE /api/cart
Authorization: Bearer {accessToken}
```

---

## 4. 注文API

### 4.1 注文作成

```
POST /api/orders
Authorization: Bearer {accessToken}
```

**リクエスト**:

```json
{
  "deliveryAddress": "東京都渋谷区..."
}
```

**レスポンス** (201 Created):

```json
{
  "success": true,
  "data": {
    "id": 1,
    "orderDate": "2024-01-01T12:00:00",
    "totalAmount": 1000.00,
    "status": "PENDING",
    "deliveryAddress": "東京都渋谷区...",
    "orderItems": [
      {
        "id": 1,
        "food": {
          "id": 1,
          "name": "ハンバーガー"
        },
        "quantity": 2,
        "price": 500.00
      }
    ]
  }
}
```

### 4.2 注文一覧取得

```
GET /api/orders?page=0&size=10
Authorization: Bearer {accessToken}
```

### 4.3 注文詳細取得

```
GET /api/orders/{id}
Authorization: Bearer {accessToken}
```

### 4.4 注文キャンセル

```
POST /api/orders/{id}/cancel
Authorization: Bearer {accessToken}
```

---

## 5. カテゴリAPI

### 5.1 カテゴリ一覧取得

```
GET /api/categories
Authorization: Bearer {accessToken}
```

### 5.2 カテゴリ作成（管理者のみ）

```
POST /api/categories
Authorization: Bearer {accessToken}
```

**リクエスト**:

```json
{
  "name": "メイン",
  "description": "メイン料理"
}
```

---

## 6. エラーレスポンス

### 6.1 共通エラーフォーマット

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "エラーメッセージ"
  },
  "timestamp": "2024-01-01T12:00:00"
}
```

### 6.2 主なエラーコード

| コード | HTTPステータス | 説明 |
|--------|---------------|------|
| `UNAUTHORIZED` | 401 | 認証エラー |
| `FORBIDDEN` | 403 | 権限エラー |
| `NOT_FOUND` | 404 | リソース未検出 |
| `INSUFFICIENT_STOCK` | 400 | 在庫不足 |
| `INVALID_REQUEST` | 400 | 不正なリクエスト |

---

## 7. Swagger UI

APIドキュメントはSwagger UIでも確認できます：

```
http://localhost:5000/swagger-ui.html
```

**機能**:

- ✅ 全エンドポイントの一覧
- ✅ リクエスト/レスポンスの詳細
- ✅ APIテスト機能
- ✅ JWT認証サポート
