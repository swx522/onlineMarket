package com.macro.mall.assistant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 电商智能客服助手服务启动类。
 *
 * <p>本服务对外提供 {@code /assistant/chat} 等接口，封装了对大语言模型（默认阿里云通义千问）的调用，
 * 并内置了对话上下文记忆、敏感词过滤、接口限流、服务降级以及 <b>真实数据库查询增强（RAG）</b> 等能力。
 *
 * <p>启动方式：
 * <pre>
 *   export DASHSCOPE_API_KEY=sk-xxxx
 *   mvn spring-boot:run
 * </pre>
 * 启动后访问 Swagger 文档：http://localhost:8085/swagger-ui/index.html
 */
@MapperScan("com.macro.mall.mapper")
@SpringBootApplication
public class MallAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallAssistantApplication.class, args);
    }
}
