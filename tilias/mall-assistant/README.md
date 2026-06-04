# mall-assistant 电商智能客服助手（后端）

> 对应《智能助手功能开发任务清单》第一阶段：后端开发（LLM 服务集成 + API 接口）。

基于 Spring Boot 的电商智能客服助手后端服务。封装了对大语言模型（默认 **阿里云通义千问**）的调用，
对外提供统一的对话接口，并内置 **对话上下文记忆、敏感词过滤、接口限流、服务降级** 等能力。

本模块为 **自包含（self-contained）** 的独立 Spring Boot 应用，可单独启动调试；包名 `com.macro.mall.assistant`
与 mall 主工程保持一致，便于后续并入主工程。

---

## 一、功能特性

| 能力 | 说明 |
| --- | --- |
| 统一对话接口 | `LlmClient` 抽象，屏蔽服务商差异，切换通义千问 / 智谱 GLM / OpenAI 只需改配置 |
| 电商客服人设 | `PromptBuilder` 内置系统提示词，让 AI 扮演电商客服，可配置覆盖 |
| 上下文记忆 | 默认进程内存，支持切换 Redis；保留最近 N 轮（默认 5 轮）对话 |
| 敏感词过滤 | 基于 Hutool DFA 词树，输入拦截 + 输出脱敏 |
| 接口限流 | 单用户每分钟最多 N 次（默认 10 次），超出返回 429 |
| 服务降级 | 大模型超时/不可用时返回兜底话术，保证接口可用 |
| Swagger 文档 | 集成 Springfox 3.0，启动后可在线调试 |

## 二、技术栈

- Spring Boot 2.7.5 / JDK 1.8
- Spring Web + Validation
- Spring Data Redis（可选）
- Hutool（DFA 敏感词、工具类）
- Springfox 3.0（Swagger）
- Lombok

## 三、目录结构

```
tilias/mall-assistant
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/macro/mall/assistant/
    │   ├── MallAssistantApplication.java     启动类
    │   ├── api/                              通用返回封装（CommonResult/ResultCode）
    │   ├── config/                           配置（AssistantProperties/RestTemplate/Swagger）
    │   ├── controller/AssistantController    对话接口
    │   ├── dto/                              请求/响应 DTO
    │   ├── exception/                        自定义异常 + 全局异常处理
    │   ├── llm/                              大模型统一接口 + 通义千问实现
    │   ├── service/                          业务编排、记忆、限流、敏感词
    │   └── support/PromptBuilder            系统提示词
    └── resources/application.yml             配置文件
```

## 四、快速开始

### 1. 申请 API Key

注册[阿里云百炼 / DashScope](https://dashscope.console.aliyun.com/)，获取 API Key（形如 `sk-xxxx`）。

### 2. 配置 Key（通过环境变量，切勿写进代码）

```bash
export DASHSCOPE_API_KEY=sk-你的key
```

### 3. 启动

```bash
cd tilias/mall-assistant
mvn spring-boot:run
```

启动后访问 Swagger 文档：<http://localhost:8085/swagger-ui/index.html>

> 未配置 Key 也能启动，但调用对话接口时会走「服务降级」，返回兜底话术。

## 五、API 文档

### 5.1 发起对话

`POST /assistant/chat`

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| message | string | 是 | 用户问题，≤2000 字 |
| sessionId | string | 否 | 会话 ID，用于维持上下文；不传则自动生成 |
| userId | string | 否 | 用户标识，用于限流维度；不传则按 IP 限流 |

示例：

```bash
curl -X POST http://localhost:8085/assistant/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"我买的手机什么时候发货？","sessionId":"sess-001","userId":"member-1001"}'
```

成功响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "sessionId": "sess-001",
    "reply": "您好~ 订单发货时间通常为付款后 48 小时内……",
    "model": "qwen-plus",
    "fallback": false,
    "timestamp": 1717480000000
  }
}
```

多轮对话：把上一轮返回的 `sessionId` 带到下一次请求即可自动携带上下文。

### 5.2 清空会话历史

`DELETE /assistant/history/{sessionId}`

```bash
curl -X DELETE http://localhost:8085/assistant/history/sess-001
```

### 5.3 健康检查

`GET /assistant/health`

### 状态码约定

| code | 含义 |
| --- | --- |
| 200 | 成功 |
| 429 | 触发限流（单用户每分钟超过上限） |
| 460 | 命中敏感词被拦截 |
| 461 | 大模型不可用（一般已自动降级为 200，此码用于显式异常场景） |
| 500 | 服务异常 |

## 六、配置说明（application.yml）

所有配置以 `assistant.*` 前缀维护，关键项：

```yaml
assistant:
  enabled: true                 # 总开关
  llm:
    provider: tongyi
    base-url: https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
    api-key: ${DASHSCOPE_API_KEY:}
    model: qwen-plus            # 可选 qwen-turbo / qwen-max
    temperature: 0.7
    max-tokens: 1024
  memory:
    type: memory                # memory 或 redis
    max-rounds: 5               # 保留最近 5 轮
  rate-limit:
    enabled: true
    max-per-minute: 10          # 单用户每分钟 10 次
  sensitive:
    enabled: true
    words: []                   # 追加自定义敏感词
```

### 切换到 Redis 记忆

```yaml
assistant:
  memory:
    type: redis
spring:
  redis:
    host: localhost
    port: 6379
```

### 切换大模型服务商

把 `base-url`、`model`、`api-key` 换成对应厂商的值即可（均兼容 OpenAI 协议）：

- 智谱 GLM：`https://open.bigmodel.cn/api/paas/v4/chat/completions`，model 如 `glm-4`
- OpenAI：`https://api.openai.com/v1/chat/completions`，model 如 `gpt-4o-mini`

## 七、安全与成本注意事项

- **API Key 安全**：仅后端持有，绝不下发前端；通过环境变量注入。
- **隐私保护**：系统提示词已要求不索取/不泄露敏感信息；请勿在 Prompt 中传入手机号、地址等。
- **成本控制**：通过 `max-tokens`、限流以及（可选）每日调用上限控制费用。
- **降级**：大模型异常时自动返回兜底话术，前端需对 `fallback=true` 做提示。

## 八、并入 mall 主工程（可选）

本模块可独立运行，也可并入 mall 多模块工程：

1. 将 `mall-assistant` 加入根 `pom.xml` 的 `<modules>`；
2. 若希望复用 mall-common 的 `CommonResult`，可删除本模块 `api` 包并改为依赖 `mall-common`；
3. 或将 `AssistantController` 与 service 直接搬入 `mall-portal` / `mall-admin`，配置合并到对应 `application.yml`。
