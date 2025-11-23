package com.fooddel.exception.handler;

import com.fooddel.dto.response.ApiResponse;
import com.fooddel.exception.custom.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * グローバル例外ハンドラー
 * 全てのコントローラーで発生した例外をここで一元的に処理します。
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * ビジネスロジック例外のハンドリング
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException ex) {
        ApiResponse<Object> errorResponse = ApiResponse.error(ex.getMessage(), ex.getCode());
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    /**
     * Bean Validation 例外のハンドリング
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        ApiResponse<Object> errorResponse = ApiResponse.error("入力内容が無効です", "INVALID_INPUT", errors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * その他のすべての例外のハンドリング（フォールバック）
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleAllExceptions(Exception ex) {
        log.error("予期せぬエラーが発生しました", ex);
        ApiResponse<Object> errorResponse = ApiResponse.error("サーバー内部でエラーが発生しました", "INTERNAL_SERVER_ERROR");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
