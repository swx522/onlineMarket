# Mall 电商平台

基于 Spring Boot 的全栈电商系统，包含后台管理、前台门户、商品检索、智能客服助手等模块。

## 项目结构

```
onlineMarket/
├── mall-common/        公共模块（通用返回封装、分页、Redis 工具等）
├── mall-mbg/           MyBatis Generator 自动生成（70+ 表的 Model + Mapper）
├── mall-security/      Spring Security + JWT 动态权限验证
├── mall-admin/         后台管理服务（商品/订单/用户/营销/内容管理）
├── mall-portal/        前台门户服务（购物车/下单/支付/会员中心）
├── mall-search/        Elasticsearch 商品全文检索服务
├── mall-demo/          演示/示例模块
└── tilias/
    └── mall-assistant/ 智能客服助手（LLM 对话 + 真实数据库检索增强）
```

## 技术栈

| 分类 | 技术 |
|------|------|
| 基础框架 | Spring Boot 2.7.5 / JDK 1.8 |
| 持久层 | MyBatis + Druid 连接池 + MySQL 8 |
| 缓存 | Redis |
| 搜索引擎 | Elasticsearch (IK 分词器) |
| 权限认证 | Spring Security + JWT + 动态 RBAC |
| 消息队列 | RabbitMQ（异步取消订单） |
| 文档型 DB | MongoDB（浏览记录/收藏/关注） |
| 对象存储 | MinIO |
| 支付 | 支付宝沙箱 |
| 云服务 | 阿里云 OSS |
| API 文档 | Springfox 3.0 (Swagger) |
| LLM 对接 | 通义千问 / OpenAI 兼容协议 |

## 各模块端口

| 模块 | 端口 | 说明 |
|------|------|------|
| mall-admin | 8080 | 后台管理 API |
| mall-portal | 8085 | 前台门户 API |
| mall-search | 8081 | 商品检索 API |
| mall-assistant | 8085 | 智能客服助手 API |

## 前置环境

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0（数据库名 `mall`）
- Redis
- Elasticsearch（可选，mall-search 需要）
- RabbitMQ（可选，mall-portal 异步取消订单需要）
- MongoDB（可选，浏览记录/收藏功能需要）
- MinIO（可选，文件上传需要）

## 快速开始

### 1. 初始化数据库

创建 MySQL 数据库 `mall`，导入 `mall-mbg` 所生成的表结构（或运行 mall-admin 让 MyBatis 自动建表），初始化基本数据。

mall-admin 默认数据源：`root / 548866`

### 2. 启动依赖服务

```bash
# 启动 Redis
redis-server

# （可选）启动 Elasticsearch
elasticsearch.bat

# （可选）启动 RabbitMQ
rabbitmq-server.bat
```

### 3. 编译 & 启动模块

```bash
# 先安装公共依赖到本地 Maven 仓库
cd onlineMarket
mvn install -pl mall-common,mall-mbg -am -DskipTests

# 启动后台管理
cd mall-admin && mvn spring-boot:run

# 启动前台门户
cd mall-portal && mvn spring-boot:run

# 启动商品检索（可选）
cd mall-search && mvn spring-boot:run

# 启动智能客服助手（可选）
cd tilias/mall-assistant && mvn spring-boot:run
```

### 4. 智能客服助手配置

智能客服助手对接通义千问大模型，需要设置 API Key：

```bash
export DASHSCOPE_API_KEY=sk-你的key
```

助手支持两种模式：
- **纯对话**：不传 `memberId`，LLM 基于训练数据回答问题
- **数据增强（RAG）**：传 `memberId`，自动查询该用户的真实订单/商品/优惠券数据，LLM 基于真实数据回答

```bash
# 纯对话
curl -X POST http://localhost:8085/assistant/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"有什么手机推荐吗"}'

# 带真实数据查询
curl -X POST http://localhost:8085/assistant/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"我的订单发货了吗","memberId":1}'
```

## API 文档

各模块启动后访问 Swagger：

| 模块 | Swagger 地址 |
|------|-------------|
| mall-admin | http://localhost:8080/swagger-ui/index.html |
| mall-portal | http://localhost:8085/swagger-ui/index.html |
| mall-search | http://localhost:8081/swagger-ui/index.html |
| mall-assistant | http://localhost:8085/swagger-ui/index.html |

## 数据库表概览

- **PMS**（商品）：商品、品牌、分类、SKU、属性、评论等 17 张表
- **OMS**（订单）：订单、订单项、购物车、退货申请等 8 张表
- **UMS**（用户）：会员、管理员、角色、权限、菜单等 18 张表
- **SMS**（营销）：优惠券、秒杀、首页推荐、广告等 11 张表
- **CMS**（内容）：帮助、专题、话题、评论等 12 张表

共 70+ 张表，均由 MyBatis Generator 自动生成 Model + Mapper + Example。

## 智能客服助手能力矩阵

| 能力 | 实现方式 |
|------|----------|
| LLM 统一抽象 | `LlmClient` 接口，切换模型只改配置 |
| 电商客服人设 | `PromptBuilder` 内置 system prompt，可定制覆盖 |
| 意图识别 | 关键词规则匹配（订单/商品/账号/优惠券/闲聊） |
| 数据库检索增强 | 查 MySQL 真实数据注入 prompt（RAG） |
| 上下文记忆 | 进程内存 LRU / Redis List 两种实现 |
| 敏感词过滤 | Hutool DFA 词树，输入拦截 + 输出脱敏 |
| 接口限流 | ConcurrentHashMap 固定窗口，单用户 N 次/分钟 |
| 服务降级 | LLM 不可用时返回兜底话术，HTTP 200 |

## 切换 LLM 服务商

`mall-assistant` 默认对接通义千问，切到 OpenAI / 智谱 GLM 只需修改 `application.yml`：

```yaml
assistant:
  llm:
    base-url: https://api.openai.com/v1/chat/completions   # 改地址
    api-key: ${OPENAI_API_KEY}                              # 改环境变量
    model: gpt-4o-mini                                      # 改模型名
```

## 安全注意事项

- API Key 通过环境变量注入，切勿硬编码到配置文件或提交到版本控制
- 智能客服的系统提示词已内置安全红线（不索取/不泄露密码、银行卡等敏感信息）
- 数据查询已限定 `memberId`，不会跨用户泄露
- 生产环境请替换 JWT secret、数据库密码等默认值
