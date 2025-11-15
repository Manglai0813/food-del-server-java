# Food Delivery Platform Backend (Java/Spring Boot)

## 概要

このプロジェクトは、Food Delivery Platform のバックエンドサーバーを Java 21 と Spring Boot 3.4.0 を使用して再実装したものです。既存の TypeScript (Bun + Express) バックエンドからの移行を目的としており、エンタープライズレベルの安定性、パフォーマンス、長期保守性を目指しています。

## 技術スタック

-   **言語**: Java 21
-   **フレームワーク**: Spring Boot 3.4.0
-   **ビルドツール**: Gradle
-   **ORM**: Spring Data JPA + Hibernate
-   **データベース**: PostgreSQL
-   **認証・認可**: Spring Security + JWT
-   **入力検証**: Bean Validation (Jakarta)
-   **ユーティリティ**: Lombok

## プロジェクト構造

```
src/main/java/com/fooddel/
├── controller/              # REST コントローラー層
├── service/                 # ビジネスサービス層
│   ├── impl/               # サービス実装クラス
│   └── interface/          # サービスインターフェース
├── repository/              # データアクセス層
├── entity/                  # JPA エンティティクラス
├── dto/                     # データ転送オブジェクト
│   ├── request/            # リクエスト DTO
│   └── response/           # レスポンス DTO
├── mapper/                  # Entity-DTO 変換器
├── security/                # セキュリティモジュール
├── exception/               # 例外処理
├── config/                  # 設定クラス
├── interceptor/             # インターセプター
├── aspect/                  # AOP アスペクト
├── constant/                # 定数定義
└── util/                    # ユーティリティクラス
```

## 開発環境のセットアップ

### 前提条件

-   Java Development Kit (JDK) 21
-   PostgreSQL データベース
-   Git

### データベース設定

`src/main/resources/application.yml` ファイルを編集し、お使いの PostgreSQL データベース接続情報を設定してください。

**開発環境 (`dev` プロファイル)**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fooddb
    username: postgres
    password: # ローカル環境でパスワードがない場合は空欄
```

### プロジェクトのビルドと実行

1.  **プロジェクトのクローン**:
    ```bash
    git clone <YOUR_REPOSITORY_URL>
    cd food-del-server-java
    ```
2.  **依存関係の解決とビルド**:
    ```bash
    ./gradlew build
    ```
3.  **アプリケーションの実行**:
    ```bash
    ./gradlew bootRun
    ```
    または、ビルドされた JAR ファイルを実行:
    ```bash
    java -jar build/libs/food-del-server-java-0.0.1-SNAPSHOT.jar
    ```

アプリケーションはデフォルトで `http://localhost:5000` で起動します。

## API ドキュメント

API の詳細な仕様については、`docs/13_API設計ドキュメント.md` を参照してください。

## 貢献

貢献に関するガイドラインは `CONTRIBUTING.md` を参照してください（未作成）。

## ライセンス

このプロジェクトは [LICENSE] の下でライセンスされています（未作成）。
