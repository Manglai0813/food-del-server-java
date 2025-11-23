package com.fooddel.config.security;

import com.fooddel.security.filter.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Securityの基本設定クラス
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // メソッドレベルのセキュリティを有効化
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;
    @Value("${app.cors.allowed-methods}")
    private String[] allowedMethods;
    @Value("${app.cors.allowed-headers}")
    private String[] allowedHeaders;
    @Value("${app.cors.allow-credentials}")
    private Boolean allowCredentials;

    // コンストラクタ
    public SecurityConfig() {
    }

    // Spring Securityのセキュリティフィルターチェーンを設定
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter,
            UserDetailsService userDetailsService) throws Exception {
        http
                // CSRF保護を無効化 (REST APIのため)
                .csrf(AbstractHttpConfigurer::disable)
                // CORS設定を適用
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // セキュリティヘッダー設定 (Helmet相当)
                .headers(headers -> headers
                        .xssProtection(
                                xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'")) // 例
                        .frameOptions(frameOptions -> frameOptions.deny())
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000)))
                // APIのアクセス権限設定
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/**").permitAll() // 認証エンドポイントは公開
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Swagger UIは公開
                        .anyRequest().authenticated() // その他のリクエストはすべて認証が必要
                )
                // セッション管理をステートレスに設定 (JWTを使用するため)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 認証プロバイダーを設定
                .authenticationProvider(authenticationProvider(userDetailsService)) // Pass UserDetailsService here
                // JWTフィルターをUsernamePasswordAuthenticationFilterの前に挿入
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS設定を適用
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(allowCredentials);
        config.setAllowedOrigins(Arrays.asList(allowedOrigins));
        config.setAllowedMethods(Arrays.asList(allowedMethods));
        config.setAllowedHeaders(Arrays.asList(allowedHeaders));
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // パスワードエンコーダーを設定
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 認証プロバイダーを設定
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) { // Inject
                                                                                                  // UserDetailsService
                                                                                                  // here
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // 認証マネージャーを設定
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
