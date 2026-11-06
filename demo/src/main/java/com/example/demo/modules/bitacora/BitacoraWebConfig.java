package com.example.demo.modules.bitacora;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class BitacoraWebConfig implements WebMvcConfigurer {

    private final BitacoraInterceptor bitacoraInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(bitacoraInterceptor).addPathPatterns("/api/v1/**");
    }
}
