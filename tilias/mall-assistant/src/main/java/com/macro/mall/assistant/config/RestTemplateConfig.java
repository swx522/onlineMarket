package com.macro.mall.assistant.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.springframework.http.converter.StringHttpMessageConverter;

/**
 * 对外调用 LLM 服务所用的 {@link RestTemplate}，超时时间取自配置。
 */
@Configuration
@EnableConfigurationProperties(AssistantProperties.class)
public class RestTemplateConfig {

    @Bean("llmRestTemplate")
    public RestTemplate llmRestTemplate(AssistantProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeout = properties.getLlm().getTimeoutMillis();
        factory.setConnectTimeout(Math.min(timeout, 10000));
        factory.setReadTimeout(timeout);
        RestTemplate restTemplate = new RestTemplate(factory);
        // 强制使用 UTF-8，避免大模型返回的中文出现乱码
        restTemplate.getMessageConverters().stream()
                .filter(c -> c instanceof StringHttpMessageConverter)
                .map(c -> (StringHttpMessageConverter) c)
                .collect(Collectors.toList())
                .forEach(c -> c.setDefaultCharset(StandardCharsets.UTF_8));
        return restTemplate;
    }
}
