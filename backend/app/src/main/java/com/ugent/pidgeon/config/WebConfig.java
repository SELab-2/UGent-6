package com.ugent.pidgeon.config;

import com.ugent.pidgeon.auth.RolesInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    private final RolesInterceptor rolesInterceptor;

    @Autowired
    public WebConfig(RolesInterceptor rolesInterceptor) {
        this.rolesInterceptor = rolesInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("*")
                .allowedOrigins("*")
                .exposedHeaders("Content-Disposition")
                .allowedHeaders("*");

    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rolesInterceptor);
    }
}