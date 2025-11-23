package com.fooddel.security.filter;

import com.fooddel.security.TokenBlacklistService;
import com.fooddel.security.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT認証フィルター
 * 全てのリクエストに対して一度だけ実行され、JWTトークンの検証と認証を行います。
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService, TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // ヘッダーが存在しない、または"Bearer "で始まらない場合は次のフィルターへ
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // "Bearer "を除去してJWTトークンを取得
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtUtil.extractUsername(jwt);

            // トークンがブラックリストに含まれているかチェック
            if (tokenBlacklistService.isTokenBlacklisted(jwtUtil.extractJti(jwt))) {
                sendUnauthorizedResponse(response, "トークンは無効化されています。", "TOKEN_BLACKLISTED");
                return;
            }

            // ユーザー名が存在し、かつSecurityContextに認証情報がまだない場合
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // データベースからユーザー情報を取得
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // トークンが有効な場合
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    // 認証トークンを作成
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // パスワードは不要
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    // SecurityContextに認証情報を設定
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            sendUnauthorizedResponse(response, "トークンの有効期限が切れています。", "TOKEN_EXPIRED");
            return;
        } catch (io.jsonwebtoken.SignatureException e) {
            sendUnauthorizedResponse(response, "トークンの署名が無効です。", "INVALID_SIGNATURE");
            return;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            sendUnauthorizedResponse(response, "トークンの形式が不正です。", "MALFORMED_TOKEN");
            return;
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            sendUnauthorizedResponse(response, "サポートされていないトークン形式です。", "UNSUPPORTED_TOKEN");
            return;
        } catch (IllegalArgumentException e) {
            sendUnauthorizedResponse(response, "トークンが空です。", "EMPTY_TOKEN");
            return;
        } catch (Exception e) {
            sendUnauthorizedResponse(response, "認証に失敗しました。", "AUTHENTICATION_FAILED");
            return;
        }

        // 次のフィルターを実行
        filterChain.doFilter(request, response);
    }

    /**
     * 401 Unauthorizedレスポンスを送信するヘルパーメソッド
     * @param response HttpServletResponse
     * @param message エラーメッセージ
     * @param code エラーコード
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message, String code) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonResponse = String.format(
                "{\"success\":false,\"message\":\"%s\",\"code\":\"%s\",\"data\":null}",
                message, code
        );
        response.getWriter().write(jsonResponse);
    }
}
