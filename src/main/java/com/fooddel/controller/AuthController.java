package com.fooddel.controller;

import com.fooddel.dto.request.LoginRequest;
import com.fooddel.dto.request.RefreshTokenRequest;
import com.fooddel.dto.request.UserRequest;
import com.fooddel.dto.response.ApiResponse;
import com.fooddel.dto.response.AuthResponse;
import com.fooddel.dto.response.UserResponse;
import com.fooddel.entity.User;
import com.fooddel.exception.custom.BusinessException;
import com.fooddel.mapper.UserMapper;
import com.fooddel.security.TokenBlacklistService;
import com.fooddel.security.jwt.JwtUtil;
import com.fooddel.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.Date;

/**
 * 認証関連のAPIエンドポイントを処理するコントローラー
 */
@Tag(name = "認証", description = "ユーザー認証関連のAPI")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthController(UserService userService, UserMapper userMapper, AuthenticationManager authenticationManager,
            JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * ユーザー登録エンドポイント
     * 
     * @param userRequest ユーザー登録情報
     * @return 作成されたユーザー情報を含むレスポンス
     */
    @Operation(summary = "ユーザー登録", description = "新しいユーザーアカウントを作成します")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "登録成功"),
            @ApiResponse(responseCode = "409", description = "メールアドレス重複")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody UserRequest userRequest) {
        // ユーザー作成サービスを呼び出す
        User createdUser = userService.createUser(userRequest);

        // EntityをResponse DTOに変換
        UserResponse userResponse = userMapper.toResponse(createdUser);

        // 成功レスポンスを生成
        ApiResponse<UserResponse> response = ApiResponse.success(userResponse, "ユーザー登録が成功しました。");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * ログインエンドポイント
     * 
     * @param loginRequest ログイン情報
     * @return JWTトークンを含むレスポンス
     */
    @Operation(summary = "ログイン", description = "メールアドレスとパスワードでログインし、JWTトークンを取得します")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "ログイン成功"),
            @ApiResponse(responseCode = "401", description = "認証失敗")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        // 認証を実行
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        // UserDetailsを取得
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // アクセストークンとリフレッシュトークンを生成
        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        // リフレッシュトークンをデータベースに保存
        userService.saveUserRefreshToken(userDetails.getUsername(), refreshToken);

        // レスポンスを生成
        AuthResponse authResponse = new AuthResponse(accessToken, refreshToken);
        ApiResponse<AuthResponse> response = ApiResponse.success(authResponse, "ログインに成功しました。");

        return ResponseEntity.ok(response);
    }

    /**
     * トークンリフレッシュエンドポイント
     * 
     * @param refreshTokenRequest リフレッシュトークンを含むリクエスト
     * @return 新しいアクセストークンを含むレスポンス
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        String requestRefreshToken = refreshTokenRequest.getRefreshToken();

        return userService.findByRefreshToken(requestRefreshToken)
                .map(user -> {
                    // ここでリフレッシュトークンの有効期限チェックなども可能
                    // JwtUtilにリフレッシュトークン検証メソッドを追加するのが望ましい

                    String newAccessToken = jwtUtil.generateToken(user);
                    AuthResponse authResponse = new AuthResponse(newAccessToken, requestRefreshToken); // 古いリフレッシュトークンをそのまま返す
                    ApiResponse<AuthResponse> response = ApiResponse.success(authResponse, "トークンが正常に更新されました。");
                    return ResponseEntity.ok(response);
                })
                .orElseThrow(
                        () -> new BusinessException(HttpStatus.FORBIDDEN, "INVALID_REFRESH_TOKEN", "無効なリフレッシュトークンです。"));
    }

    /**
     * ログアウトエンドポイント
     * 
     * @param request HttpServletRequest
     * @return ログアウト成功レスポンス
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logoutUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            String jti = jwtUtil.extractJti(jwt);
            Date expiration = jwtUtil.extractExpiration(jwt);
            long expirationSeconds = (expiration.getTime() - System.currentTimeMillis()) / 1000;

            if (expirationSeconds > 0) {
                tokenBlacklistService.blacklistToken(jti, expirationSeconds);
            }
        }
        // SecurityContextをクリア
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.success("ログアウトしました。"));
    }
}
