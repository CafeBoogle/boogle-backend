package com.boogle.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.stream.Collectors;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Boogle API 명세서")
                        .description("회원가입 및 이미지 업로드 테스트")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("테스트용 토큰을 입력하거나, 비워두면 필터에서 가짜 유저로 처리합니다.")));

    }
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("boogle-api")
                .pathsToMatch("/api/**")
                .packagesToScan("com.boogle.controller") // 컨트롤러가 위치한 패키지 경로
                // 태그 순서를 사전순(알파벳/숫자 순)으로 정렬
                .addOpenApiCustomizer(openApi -> openApi.setTags(
                        openApi.getTags().stream()
                                .sorted(Comparator.comparing(Tag::getName))
                                .collect(Collectors.toList())
                ))
                .build();
    }
}