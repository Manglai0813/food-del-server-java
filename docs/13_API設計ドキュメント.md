# API 設計ドキュメント

## 概要

Food Delivery Platform のすべての API エンドポイントの詳細仕様を説明するドキュメントです。

## API 基本仕様

### ベース URL

- **開発環境**: `http://localhost:5000`
- **本番環境**: 環境変数から取得

### レスポンス形式

すべての API は統一された `ApiResponse<T>` 形式で返却されます。

**成功レスポンス**:
```json
{
  "success": true,
  "message": "操作が成功しました",
  "data": { ... }
}
```

**エラーレスポンス**:
```json
{
  "success": false,
  "message": "エラーメッセージ",
  "code": "ERROR_CODE",
  "errors": ["エラー詳細1", "エラー詳細2"]
}
```

### 認証

**認証方式**: JWT Bearer Token

**ヘッダー**:
```
Authorization: Bearer <access_token>
```

---

## 認証・ユーザー管理 API

### 1. ユーザー登録

**エンドポイント**: `POST /api/users/auth/register`

**認証**: 不要

**リクエストボディ**:
```json
{
  "name": "ユーザー名",
  "email": "user@example.com",
  "password": "password123",
  "phone": "090-1234-5678"
}
```

**レスポンス** (200 OK):
```json
{
  "success": true,
  "message": "ユーザー登録が成功しました",
  "data": {
    "user": { ... },
    "token": "...",
    "refreshToken": "..."
  }
}
```

---

### 2. ログイン

**エンドポイント**: `POST /api/users/auth/login`

**認証**: 不要

**リクエストボディ**:
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

---

## 商品管理 API

### 1. 商品一覧取得（公開用）

**エンドポイント**: `GET /api/foods`

**認証**: 不要

**クエリパラメータ**:
- `page`: ページ番号（デフォルト: 1）
- `limit`: 件数（デフォルト: 10）
- `keyword`: キーワード検索
- `categoryId`: カテゴリID
- `status`: ステータス（true/false）
- `minPrice`: 最低価格
- `maxPrice`: 最高価格
- `sortBy`: ソート項目（price, name, created_at）
- `sortOrder`: ソート順（asc, desc）

**レスポンス** (200 OK):
```json
{
  "success": true,
  "message": "商品リストを取得しました",
  "data": [ ... ],
  "pagination": { ... }
}
```

---

### 2. 商品詳細取得

**エンドポイント**: `GET /api/foods/:id`

**認証**: 不要

---

## ショッピングカート API

### 1. カート取得

**エンドポイント**: `GET /api/carts`

**認証**: 必須

---

### 2. 商品追加

**エンドポイント**: `POST /api/carts/add`

**認証**: 必須

**リクエストボディ**:
```json
{
  "foodId": 1,
  "quantity": 2
}
```

---

## 注文管理 API

### 1. 注文一覧取得

**エンドポイント**: `GET /api/orders`

**認証**: 必須

---

### 2. 注文詳細取得

**エンドポイント**: `GET /api/orders/:id`

**認証**: 必須

---

## エラーコード一覧

### 認証エラー

- `UNAUTHORIZED`: 認証が必要
- `TOKEN_EXPIRED`: トークン期限切れ
- `TOKEN_INVALID`: トークン無効

### ビジネスエラー

- `STOCK_INSUFFICIENT`: 在庫不足
- `ORDER_NOT_FOUND`: 注文不存在
- `CART_EMPTY`: カートが空

---

## HTTP ステータスコード

- **200 OK**: 成功
- **201 Created**: 作成成功
- **400 Bad Request**: リクエストエラー
- **401 Unauthorized**: 認証エラー
- **403 Forbidden**: 権限不足
- **404 Not Found**: リソース不存在
- **500 Internal Server Error**: サーバーエラー

---

**作成日**: 2025-01-XX  
**バージョン**: 1.0

