package com.fooddel.service.impl;

import com.fooddel.exception.custom.BusinessException;
import com.fooddel.service.FileService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * ファイル管理関連のビジネスロジックを実装するサービス
 */
@Service
public class FileServiceImpl implements FileService {

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    private Path fileStorageLocation;

    // 許可されるファイル拡張子
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    // 最大ファイルサイズ (5MB) - Spring Bootの設定でも制限可能だが、ここでもチェック
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("アップロードディレクトリを作成できませんでした。", ex);
        }
    }

    // 商品画像を保存します。
    @Override
    public String saveProductImage(MultipartFile file) {
        validateFile(file);

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(originalFileName);

        // ユニークなファイル名を生成
        String newFileName = UUID.randomUUID().toString() + "." + extension;

        try {
            // ファイル名に不正な文字が含まれていないかチェック
            if (newFileName.contains("..")) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_FILE_PATH",
                        "ファイル名に不正なパスが含まれています: " + newFileName);
            }

            // ファイルを保存
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            // 相対パスを返す (/uploads/filename.jpg)
            return "/uploads/" + newFileName;

        } catch (IOException ex) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_UPLOAD_FAILED",
                    "ファイルの保存に失敗しました: " + newFileName);
        }
    }

    // 指定されたパスのファイルを削除します。
    @Override
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        try {
            // パスからファイル名を抽出 (/uploads/filename.jpg -> filename.jpg)
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.deleteIfExists(targetLocation);
        } catch (IOException ex) {
            // 削除失敗はログに出力する程度で、処理は継続させる（または例外を投げるか要件による）
            // log.error("ファイルの削除に失敗しました: " + filePath, ex);
        }
    }

    // ファイルの検証を行います。
    @Override
    public void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "FILE_EMPTY", "ファイルが空です。");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "FILE_TOO_LARGE", "ファイルサイズが大きすぎます。最大5MBまでです。");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_FILE_NAME", "ファイル名が無効です。");
        }

        String extension = getFileExtension(originalFileName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_FILE_TYPE",
                    "許可されていないファイル形式です。画像ファイルのみアップロード可能です。");
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }
}
