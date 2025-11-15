package com.fooddel.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 統一APIレスポンス形式
 * 全てのAPIレスポンスはこのクラスでラップされます。
 * @param <T> レスポンスデータの型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String code;
    private List<String> errors;

    // 静的ファクトリーメソッ

    /**
     * 成功レスポンスを生成します。
     * @param data レスポンスデータ
     * @param message メッセージ
     * @return ApiResponseインスタンス
     * @param <T> データ型
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 成功レスポンスを生成します（データなし）。
     * @param message メッセージ
     * @return ApiResponseインスタンス
     * @param <T> データ型
     */
    public static <T> ApiResponse<T> success(String message) {
        return success(null, message);
    }

    /**
     * エラーレスポンスを生成します。
     * @param message エラーメッセージ
     * @param code エラーコード
     * @param errors エラー詳細リスト
     * @return ApiResponseインスタンス
     * @param <T> データ型
     */
    public static <T> ApiResponse<T> error(String message, String code, List<String> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .code(code)
                .errors(errors)
                .build();
    }

    /**
     * エラーレスポンスを生成します（エラー詳細なし）。
     * @param message エラーメッセージ
     * @param code エラーコード
     * @return ApiResponseインスタンス
     * @param <T> データ型
     */
    public static <T> ApiResponse<T> error(String message, String code) {
        return error(message, code, null);
    }
}
