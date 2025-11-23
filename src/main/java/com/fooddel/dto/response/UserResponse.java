package com.fooddel.dto.response;

import com.fooddel.constant.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ユーザー情報レスポンス用のDTO
 * パスワードなどの機密情報は含みません。
 */
@Data
public class UserResponse {
    private Integer id;
    private String name;
    private String email;
    private UserRole role;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
