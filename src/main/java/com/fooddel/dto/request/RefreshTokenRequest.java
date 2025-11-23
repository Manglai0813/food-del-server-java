package com.fooddel.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * トークンリフレッシュリクエスト用のDTO
 */
@Data
public class RefreshTokenRequest {

    @NotBlank(message = "リフレッシュトークンは必須です")
    private String refreshToken;
}
