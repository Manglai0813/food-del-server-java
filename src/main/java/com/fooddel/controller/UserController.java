package com.fooddel.controller;

import com.fooddel.dto.request.UserRequest;
import com.fooddel.dto.response.ApiResponse;
import com.fooddel.dto.response.PaginatedResponse;
import com.fooddel.dto.response.UserResponse;
import com.fooddel.entity.User;
import com.fooddel.mapper.UserMapper;
import com.fooddel.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ユーザープロフィール管理関連のAPIエンドポイントを処理するコントローラー
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    /**
     * 認証済みユーザーのプロフィール情報を取得します。
     * @param user 認証済みユーザー
     * @return ユーザープロフィール情報
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(@AuthenticationPrincipal User user) {
        // @AuthenticationPrincipalが直接Userエンティティを解決してくれる
        UserResponse userResponse = userMapper.toResponse(user);
        ApiResponse<UserResponse> response = ApiResponse.success(userResponse, "プロフィール情報を取得しました。");
        return ResponseEntity.ok(response);
    }

    /**
     * 認証済みユーザーのプロフィール情報を更新します。
     * @param user 認証済みユーザー
     * @param userRequest 更新するユーザー情報
     * @return 更新されたユーザープロフィール情報
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserRequest userRequest
    ) {
        User updatedUser = userService.updateUser(user.getId(), userRequest);
        UserResponse userResponse = userMapper.toResponse(updatedUser);
        ApiResponse<UserResponse> response = ApiResponse.success(userResponse, "プロフィールを更新しました。");
        return ResponseEntity.ok(response);
    }

    /**
     * 全てのユーザーをページネーション付きで取得します（管理者用）。
     * @param pageable ページネーション情報
     * @return ユーザーのページ
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaginatedResponse<UserResponse>>> getAllUsers(Pageable pageable) {
        Page<User> userPage = userService.findAllUsers(pageable);
        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        PaginatedResponse<UserResponse> paginatedData = new PaginatedResponse<>(
                userResponses,
                new com.fooddel.dto.response.PaginationInfo(
                        userPage.getNumber(),
                        userPage.getSize(),
                        userPage.getTotalElements(),
                        userPage.getTotalPages(),
                        userPage.hasNext(),
                        userPage.hasPrevious()
                )
        );

        ApiResponse<PaginatedResponse<UserResponse>> response = ApiResponse.success(paginatedData, "ユーザーリストが正常に取得されました。");
        return ResponseEntity.ok(response);
    }
}
