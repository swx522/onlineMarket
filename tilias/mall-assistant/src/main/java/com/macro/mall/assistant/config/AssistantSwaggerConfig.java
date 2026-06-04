package com.macro.mall.assistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger（Springfox 3.0）API 文档配置。
 *
 * <p>访问地址：http://localhost:8085/swagger-ui/index.html
 *
 * <p>注意：Springfox 3.0 与 Spring Boot 2.7 共用时，需要在 application.yml 中设置
 * {@code spring.mvc.pathmatch.matching-strategy=ant_path_matcher}，本模块已配置。
 */
@Configuration
@EnableSwagger2
public class AssistantSwaggerConfig {

    @Bean
    public Docket assistantApiDocket() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.macro.mall.assistant.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Mall 智能客服助手 API")
                .description("电商智能客服助手后端接口文档：商品 / 订单 / 售后等问题的 LLM 对话能力")
                .version("1.0.0")
                .build();
    }
}
