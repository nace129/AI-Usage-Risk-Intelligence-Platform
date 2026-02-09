package com.intelligence.capture.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*") // DEV ONLY. Lock to chrome-extension://<id> in Phase 6
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*");
    }

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter f = new CommonsRequestLoggingFilter();
        f.setIncludeClientInfo(true);
        f.setIncludeQueryString(true);
        f.setIncludePayload(true);
        f.setIncludeHeaders(false);
        f.setMaxPayloadLength(10_000);
        return f;
    }
}

