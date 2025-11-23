package com.fooddel.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * ファイル管理関連のビジネスロジックを定義するサービスインターフェース
 */
public interface FileService {

    // 商品画像を保存します。
    String saveProductImage(MultipartFile file);

    // 指定されたパスのファイルを削除します。
    void deleteFile(String filePath);

    // ファイルの検証を行います。
    void validateFile(MultipartFile file);
}
