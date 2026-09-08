package com.eldersphere.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Serves locally-stored uploads (caretaker photos, medical record documents) directly off
 * disk at /uploads/** — the deliberate simplification in place of the reference project's
 * Cloudinary integration. See README "Scope decisions".
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.file-storage.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + new File(uploadDir).getAbsolutePath() + File.separator;
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}
