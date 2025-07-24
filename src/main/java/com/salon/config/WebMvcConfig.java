package com.salon.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.des-img-path}")
    private String desImgPath;

    @Value("${upload.shop-img-path}")
    private String shopImgPath;

    @Value("${upload.shop-service-img-path}")
    private String shopServiceImgPath;

    @Value("${upload.review-img-path}")
    private String reviewImgPath;

    @Value("${upload.banner-img-path}")
    private String bannerImgPath;

    @Value("${upload.anc-file-path}")
    private String ancFilePath;

    @Value("${upload.cs-file-path}")
    private String csFilePath;


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry
                .addResourceHandler("/desImg/**")
                .addResourceLocations(desImgPath);

        registry
                .addResourceHandler("/shopImg/**")
                .addResourceLocations(shopImgPath);

        registry
                .addResourceHandler("/shopServiceImg/**")
                .addResourceLocations(shopServiceImgPath);

        registry
                .addResourceHandler("/reviewImg/**")
                .addResourceLocations(reviewImgPath);

        registry
                .addResourceHandler("/bannerImg/**")
                .addResourceLocations(bannerImgPath);

        registry
                .addResourceHandler("/ancFile/**")
                .addResourceLocations(ancFilePath);

        registry
                .addResourceHandler("/csFile/**")
                .addResourceLocations(csFilePath);
    }


}


