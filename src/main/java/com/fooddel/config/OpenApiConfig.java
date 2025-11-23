package com.fooddel.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI設定クラス
 * API仕様書の自動生成とSwagger UIの設定を行います。
 */
@Configuration
public class OpenApiConfig {

    /**
     * OpenAPI仕様の設定
     * 
     * @return OpenAPIオブジェクト
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Food Delivery Platform API")
                        .version("1.0.0")
                        .description("フードデリバリープラットフォームのREST API仕様書")
                        .contact(new Contact()
                                .name("Food Delivery Team")
                                .email("support@fooddel.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWTトークンを使用した認証。ヘッダーに 'Authorization: Bearer {token}' を設定してください。")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
