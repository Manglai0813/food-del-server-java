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
        final String jwt;
        final String userEmail;

        // ヘッダーが存在しない、または"Bearer "で始まらない場合は次のフィルターへ
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // "Bearer "を除去してJWTトークンを取得
        jwt = authHeader.substring(7);
        userEmail = jwtUtil.extractUsername(jwt);

        // トークンがブラックリストに含まれているかチェック
        if (tokenBlacklistService.isTokenBlacklisted(jwtUtil.extractJti(jwt))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
            response.getWriter().write("トークンは無効化されています。");
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
        // 次のフィルターを実行
        filterChain.doFilter(request, response);
    }
}
