package com.boogle.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 리뷰
        registry
                .addResourceHandler("/images/reviews/**")
                .addResourceLocations("file:/home/ubuntu/app/uploads/reviews/");

        //프로필
        registry
                .addResourceHandler("/images/profiles/**")
                .addResourceLocations("file:/home/ubuntu/app/uploads/profiles/");
    }
}
