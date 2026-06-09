package com.macro.mall.assistant.config;

import com.macro.mall.common.config.BaseSwaggerConfig;
import com.macro.mall.common.domain.SwaggerProperties;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger API 文档配置
 */
@Configuration
@EnableSwagger2
public class AssistantSwaggerConfig extends BaseSwaggerConfig {

    @Override
    public SwaggerProperties swaggerProperties() {
        return SwaggerProperties.builder()
                .apiBasePackage("com.macro.mall.assistant.controller")
                .title("mall智能客服助手")
                .description("电商智能客服助手后端接口文档")
                .contactName("macro")
                .version("1.0")
                .enableSecurity(false)
                .build();
    }

    @Bean
    public BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return generateBeanPostProcessor();
    }
}
