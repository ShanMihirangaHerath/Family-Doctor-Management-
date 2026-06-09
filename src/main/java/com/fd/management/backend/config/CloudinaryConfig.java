package com.fd.management.backend.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dzytaxqil");
        config.put("api_key", "513146965555618");
        config.put("api_secret", "wjCmLDIcUqx8p0J7y2-hg5ltifU");
        return new Cloudinary(config);
    }
}