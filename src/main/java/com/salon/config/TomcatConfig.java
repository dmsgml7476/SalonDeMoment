package com.salon.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    private static final Logger logger = LoggerFactory.getLogger(TomcatConfig.class); // Logger 인스턴스 생성


    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            // 전체 파라미터(폼 필드+파일) 제한을 크게 늘립니다
            connector.setProperty("maxParameterCount", "200");
            // 필요하다면 fileCountMax 값도 직접 건드릴 수 있는 톰캣 버전이라면 아래처럼
            // connector.setProperty("maxFileCount", "50");
        });
    }
}
