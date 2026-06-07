# Mall 电商平台

基于 Spring Boot 的全栈电商系统，包含后台管理、前台门户、商品检索、智能客服助手等模块。

## 项目结构

```
onlineMarket/
├── mall-common/          公共模块（通用封装、Redis 工具、日志切面）
├── mall-mbg/             MyBatis Generator 自动生成（70+ 表的 Model/Mapper）
├── mall-security/        Spring Security + JWT 动态权限框架
├── mall-admin/           后台管理服务（31 个控制器，商品/订单/用户/营销管理）
├── mall-portal/          前台门户服务（购物车/下单/支付/会员中心，13 个控制器）
├── mall-search/          Elasticsearch 商品全文检索服务
├── mall-demo/            演示/示例模块
└── tilias/
    └── mall-assistant/   智能客服助手（LLM 对话 + 数据库检索增强 RAG）
```

## 技术栈

| 分类 | 技术 |
|------|------|
| 基础框架 | Spring Boot 2.7.5 / JDK 1.8 |
| 持久层 | MyBatis + Druid 连接池 + MySQL 8.0 |
| 分页 | PageHelper |
| 缓存 | Redis（Spring Data Redis） |
| 搜索引擎 | Elasticsearch 7.x（Spring Data ES + IK 分词器） |
| 权限认证 | Spring Security + JWT + 动态 URL 权限过滤 |
| 消息队列 | RabbitMQ（异步取消超时订单） |
| 文档型 DB | MongoDB（浏览记录/收藏/关注） |
| 对象存储 | MinIO / 阿里云 OSS |
| 支付 | 支付宝沙箱（PC 网页 + 移动网页） |
| API 文档 | Springfox 3.0 (Swagger / OpenAPI 3.0) |
| 日志 | Logstash + Logback |
| LLM 对接 | 通义千问 / OpenAI 兼容协议 |
| 工具库 | Lombok, Hutool |

## 各模块端口

| 模块 | 端口 | 说明 |
|------|------|------|
| mall-admin | 8080 | 后台管理 API |
| mall-portal | 8085 | 前台门户 API |
| mall-search | 8081 | 商品检索 API |
| mall-assistant | 8086 | 智能客服助手 API |

## 前置环境

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0（数据库名 `mall`，默认 root / 548866）
- Redis
- Elasticsearch（可选，mall-search 需要）
- RabbitMQ（可选，订单超时取消需要）
- MongoDB（可选，浏览记录/收藏需要）
- MinIO（可选，文件上传需要）

---

## 一、mall-common — 公共模块

基础包路径 `com.macro.mall.common`，是所有模块的共享基础，提供：

| 类 | 说明 |
|------|------|
| `CommonResult<T>` | 通用 REST 响应封装，工厂方法：`success(data)`, `failed(message)`, `unauthorized(data)`, `forbidden(data)` |
| `CommonPage<T>` | 通用分页封装，自动适配 PageHelper 和 Spring Data Page |
| `ResultCode` | 状态码枚举：`SUCCESS(200)`, `FAILED(500)`, `VALIDATE_FAILED(404)`, `UNAUTHORIZED(401)`, `FORBIDDEN(403)` |
| `RedisService` | Redis 操作统一接口，封装 String/Hash/Set/List 全部常用操作 |
| `WebLogAspect` | AOP 切面，自动记录每个请求的 URL、参数、IP、耗时，输出到 Logstash |
| `GlobalExceptionHandler` | 全局异常处理，统一返回 `CommonResult` |
| `BaseSwaggerConfig` | Swagger 基础配置抽象类，支持 JWT 认证头 |

---

## 二、mall-mbg — 数据层

MyBatis Generator 自动生成的 Model、Mapper 接口和 XML 映射文件，覆盖 `mall` 数据库中全部表：

- **PMS**（商品系统）：商品、品牌、分类、SKU、属性、属性分类、评论、相册 等 **17 张表**
- **OMS**（订单系统）：订单、订单项、购物车、退货申请、退货原因、订单设置 等 **8 张表**
- **UMS**（用户系统）：管理员、角色、菜单、资源、会员、会员等级、地址 等 **18 张表**
- **SMS**（营销系统）：优惠券、秒杀、秒杀场次、首页广告、首页推荐 等 **11 张表**
- **CMS**（内容系统）：帮助、帮助分类、专题、话题、评论、优选专区 等 **12 张表**

共 70+ 张表。每张表有对应的 `XxxMapper.java`、`XxxMapper.xml`、`Xxx.java`（Model）和 `XxxExample.java`（MyBatis Criteria 查询构造器）。

---

## 三、mall-security — 安全框架

基础包路径 `com.macro.mall.security`，可复用的安全框架：

| 组件 | 说明 |
|------|------|
| `JwtTokenUtil` | JWT 生成/验证，HS512 签名，支持 Token 刷新（30 分钟冷却） |
| `JwtAuthenticationTokenFilter` | 从 `Authorization: Bearer <token>` 头读取 JWT 并设置安全上下文 |
| `DynamicSecurityMetadataSource` | 从数据库动态加载 URL → 权限映射，运行时变更无需重启 |
| `DynamicAccessDecisionManager` | 基于用户角色权限和 URL 配置的访问决策 |
| `DynamicSecurityFilter` | 动态权限拦截过滤器，OPTIONS 直通 + 白名单 + 权限校验 |
| `SecurityConfig` | Spring Security 主配置：无状态 Session + JWT 过滤器链 |
| `RedisCacheAspect` | Redis 降级切面：Redis 宕机时自动吞掉缓存异常，不中断业务 |
| `RestfulAccessDeniedHandler` | 认证/授权失败时返回 JSON 而非重定向到登录页 |

---

## 四、mall-admin — 后台管理服务

基础包路径 `com.macro.mall`，31 个控制器，面向运营/商家的后台管理 API。

### 用户权限管理

| 控制器 | 路径 | 功能 |
|--------|------|------|
| `UmsAdminController` | `/admin` | 管理员注册、登录、列表、信息、角色分配 |
| `UmsRoleController` | `/role` | 角色 CRUD、菜单/资源分配 |
| `UmsMenuController` | `/menu` | 菜单 CRUD、树形列表、显示/隐藏 |
| `UmsResourceController` | `/resource` | 资源 CRUD（后台权限点） |
| `UmsResourceCategoryController` | `/resourceCategory` | 资源分类管理 |
| `UmsMemberLevelController` | `/memberLevel` | 会员等级列表 |

### 商品管理

| 控制器 | 路径 | 功能 |
|--------|------|------|
| `PmsProductController` | `/product` | 商品 CRUD、上下架、推荐/新品/审核状态批量更新 |
| `PmsBrandController` | `/brand` | 品牌 CRUD、显示/制造商状态批量更新 |
| `PmsProductCategoryController` | `/productCategory` | 商品分类 CRUD、树形结构、导航/显示状态 |
| `PmsProductAttributeController` | `/productAttribute` | 商品属性/规格 CRUD |
| `PmsProductAttributeCategoryController` | `/productAttribute/category` | 属性分类管理 |
| `PmsSkuStockController` | `/sku` | SKU 库存查看和批量更新 |

### 订单管理

| 控制器 | 路径 | 功能 |
|--------|------|------|
| `OmsOrderController` | `/order` | 订单列表/详情、批量发货、关闭、删除、修改收货/费用信息 |
| `OmsOrderReturnApplyController` | `/returnApply` | 退货申请列表/详情、审批 |
| `OmsOrderReturnReasonController` | `/returnReason` | 退货原因 CRUD |
| `OmsOrderSettingController` | `/orderSetting` | 订单设置（超时、确认、完成时限） |
| `OmsCompanyAddressController` | `/companyAddress` | 发货地址列表 |

### 营销管理

| 控制器 | 路径 | 功能 |
|--------|------|------|
| `SmsCouponController` | `/coupon` | 优惠券 CRUD（支持全场/指定分类/指定商品） |
| `SmsCouponHistoryController` | `/couponHistory` | 优惠券领取/使用记录 |
| `SmsFlashPromotionController` | `/flash` | 秒杀活动 CRUD |
| `SmsFlashPromotionSessionController` | `/flashSession` | 秒杀场次管理 |
| `SmsFlashPromotionProductRelationController` | `/flashProductRelation` | 秒杀商品关联 |

### 首页内容管理

| 控制器 | 路径 | 功能 |
|--------|------|------|
| `SmsHomeAdvertiseController` | `/home/advertise` | 首页轮播广告 CRUD |
| `SmsHomeBrandController` | `/home/brand` | 首页推荐品牌管理 |
| `SmsHomeNewProductController` | `/home/newProduct` | 首页新品管理 |
| `SmsHomeRecommendProductController` | `/home/recommendProduct` | 首页人气推荐管理 |
| `SmsHomeRecommendSubjectController` | `/home/recommendSubject` | 首页推荐专题管理 |
| `CmsSubjectController` | `/subject` | 商品专题/话题管理 |
| `CmsPrefrenceAreaController` | `/prefrenceArea` | 商品优选专区 |

### 文件存储

| 控制器 | 路径 | 功能 |
|--------|------|------|
| `OssController` | `/aliyun/oss` | 阿里云 OSS 上传签名/回调 |
| `MinioController` | `/minio` | MinIO 文件上传/删除 |

---

## 五、mall-portal — 前台门户服务

基础包路径 `com.macro.mall.portal`，13 个控制器，面向消费者的商城前台 API。

### 会员系统

| 控制器 | 路径 | 功能 |
|--------|------|------|
| `UmsMemberController` | `/sso` | 会员注册/登录/获取验证码/修改密码/JWT Token 刷新 |
| `UmsMemberReceiveAddressController` | `/member/address` | 收货地址 CRUD |
| `UmsMemberCouponController` | `/member/coupon` | 领券、优惠券列表、可用优惠券查询 |
| `MemberAttentionController` | `/member/attention` | 品牌关注（MongoDB） |
| `MemberProductCollectionController` | `/member/productCollection` | 商品收藏（MongoDB） |
| `MemberReadHistoryController` | `/member/readHistory` | 浏览记录（MongoDB） |

### 商品浏览

| 控制器 | 路径 | 功能 |
|--------|------|------|
| `HomeController` | `/home` | 首页聚合内容（广告/秒杀/新品/推荐/专题） |
| `PmsPortalProductController` | `/product` | 商品搜索/分类树/商品详情（含属性/SKU/促销） |
| `PmsPortalBrandController` | `/brand` | 推荐品牌/品牌详情/品牌商品列表 |

### 购物 & 下单

| 控制器 | 路径 | 功能 |
|--------|------|------|
| `OmsCartItemController` | `/cart` | 购物车 CRUD、改规格/数量、促销价格计算 |
| `OmsPortalOrderController` | `/order` | 订单确认、下单、支付回调、取消/确认收货/删除 |
| `OmsPortalOrderReturnApplyController` | `/returnApply` | 申请退货 |

### 支付

| 控制器 | 路径 | 功能 |
|--------|------|------|
| `AlipayController` | `/alipay` | 支付宝 PC 网页支付/手机网页支付/异步通知/交易查询 |

### 下单全流程

```
加购物车 → 查看购物车(自动计算促销价) → 生成确认单(选地址/优惠券/积分) 
→ 提交订单(锁库存) → 支付宝支付 → 支付成功回调(扣库存) → 查看订单 → 确认收货
                                                    ↓
                                          超时未付 → RabbitMQ 延迟消息 → 自动取消(释放库存)
```

### 促销能力

- **秒杀**：限时限量特价活动
- **优惠券**：全场通用 / 指定分类 / 指定商品，满减/折扣/代金
- **会员价**：不同会员等级不同价格
- **阶梯价**：买多件享折扣
- **满减**：满 N 元减 M 元
- **积分**：下单赚积分，下单可用积分抵扣

---

## 六、mall-search — 商品检索服务

基础包路径 `com.macro.mall.search`，基于 Elasticsearch 的商品全文搜索。

| 接口 | 方法 | 说明 |
|------|------|------|
| `/esProduct/importAll` | POST | 全量同步 MySQL 商品到 ES |
| `/esProduct/create/{id}` | POST | 同步单个商品到 ES |
| `/esProduct/search/simple` | GET | 简单关键词搜索 |
| `/esProduct/search` | GET | 组合搜索（关键词 + 品牌 + 分类 + 排序） |
| `/esProduct/recommend/{id}` | GET | 基于商品的相似推荐 |
| `/esProduct/search/relate` | GET | 搜索聚合（品牌/分类/属性筛选器） |
| `/esProduct/delete/{id}` | GET | 从 ES 删除商品 |
| `/esProduct/delete/batch` | POST | 批量从 ES 删除 |

排序支持：综合相关性、最新、销量、价格升序/降序。搜索字段包括商品名称（权重 10×）、副标题（5×）、关键词（2×），使用 IK 中文分词。

---

## 七、mall-demo — 演示模块

基础包路径 `com.macro.mall.demo`，展示如何基于 mall 框架开发。包含 `DemoController`（品牌 CRUD 示例）、`RestTemplateDemoController`（RestTemplate 各种用法演示），以及自定义校验注解 `FlagValidator` 等参考代码。

---

## 八、mall-assistant — 智能客服助手

基础包路径 `com.macro.mall.assistant`，独立模块，位于 `tilias/` 目录下。默认端口 **8085**。

### API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/assistant/chat` | 发起对话 |
| `DELETE` | `/assistant/history/{sessionId}` | 清空会话历史 |
| `GET` | `/assistant/health` | 健康检查 |

### Chat 请求参数

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `message` | string | √ | 用户问题，≤2000 字 |
| `sessionId` | string | × | 会话 ID，同一 ID 多轮对话时 AI 记住上下文 |
| `userId` | string | × | 用户标识，用于限流维度，不传按 IP |
| `memberId` | Long | × | 数据库真实会员 ID，传后自动查询该用户真实数据 |

### Chat 响应字段

| 字段 | 说明 |
|------|------|
| `reply` | AI 回复内容 |
| `sessionId` | 会话 ID，下一轮带上维持上下文 |
| `model` | 本次使用的模型名 |
| `fallback` | true 表示 LLM 不可用，返回兜底话术 |
| `timestamp` | 回复时间戳 |

### 调用示例

```bash
# 纯对话模式（不传 memberId，LLM 基于训练数据回答）
curl -X POST http://localhost:8085/assistant/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"有什么手机推荐吗"}'

# 多轮对话（带 sessionId 维持上下文）
# 第 1 轮
curl -X POST http://localhost:8085/assistant/chat \
  -d '{"message":"有什么手机推荐吗"}'
# → 返回 "sessionId":"sess-abc123"

# 第 2 轮
curl -X POST http://localhost:8085/assistant/chat \
  -d '{"message":"第一个多少钱","sessionId":"sess-abc123"}'
# → AI 知道"第一个"指的是上一轮推荐的手机

# 数据增强模式（RAG，传 memberId 查真实数据）
curl -X POST http://localhost:8085/assistant/chat \
  -d '{"message":"我的订单发货了吗","memberId":1}'
# → AI 自动查 ums_member 表该用户的真实订单/积分/优惠券，基于真实数据回答
```

### 能力矩阵

| 能力 | 实现方式 |
|------|----------|
| LLM 统一抽象 | `LlmClient` 接口，切换模型只改配置 |
| 电商客服人设 | `PromptBuilder` 内置 system prompt，可配置覆盖 |
| 意图识别 | 关键词规则匹配（订单/商品/账号/优惠券/闲聊 5 类） |
| 数据库检索增强 | 查 MySQL 真实数据注入 prompt（RAG） |
| 上下文记忆 | 进程内存 LRU / Redis List 两种实现 |
| 敏感词过滤 | Hutool DFA 词树，输入拦截 + 输出脱敏 |
| 接口限流 | ConcurrentHashMap 固定窗口，单用户 N 次/分钟 |
| 服务降级 | LLM 不可用时返回兜底话术，HTTP 200 |

### 架构设计

```
用户消息 → 限流 → 敏感词过滤 → 意图识别 → 查数据库(RAG) 
→ 注入数据到 prompt → 加载历史 → 调 LLM → 回写记忆 → 返回
```

`LlmClient` 接口屏蔽了 LLM 厂商差异。默认实现 `TongYiLlmClient` 对接通义千问 OpenAI 兼容接口，换到 OpenAI / 智谱 GLM 只需改 `application.yml`：

```yaml
assistant:
  llm:
    base-url: https://api.openai.com/v1/chat/completions
    api-key: ${OPENAI_API_KEY}
    model: gpt-4o-mini
```

---

## 快速开始

### 1. 初始化数据库

创建 MySQL 数据库 `mall`，导入表结构。默认数据源 `root / 548866`（与 mall-admin 的 application-dev.yml 一致）。

### 2. 启动依赖服务

```bash
redis-server                          # 必须
elasticsearch.bat                     # 可选，mall-search 需要
rabbitmq-server.bat                   # 可选，订单超时取消需要
```

### 3. 编译 & 启动

```bash
# 先安装公共依赖到本地 Maven 仓库
cd onlineMarket
mvn install -pl mall-common,mall-mbg -am -DskipTests

# 后台管理
cd mall-admin && mvn spring-boot:run

# 前台门户
cd mall-portal && mvn spring-boot:run

# 商品检索（可选）
cd mall-search && mvn spring-boot:run

# 智能客服助手（可选，需要设置 API Key）
export DASHSCOPE_API_KEY=sk-你的key
cd tilias/mall-assistant && mvn spring-boot:run
```

### 4. 访问 Swagger 文档

| 模块 | 地址 |
|------|------|
| mall-admin | http://localhost:8080/swagger-ui/index.html |
| mall-portal | http://localhost:8085/swagger-ui/index.html |
| mall-search | http://localhost:8081/swagger-ui/index.html |
| mall-assistant | http://localhost:8085/swagger-ui/index.html |

## 安全注意事项

- API Key / JWT Secret / 数据库密码 应通过环境变量注入，切勿硬编码提交到 Git
- 智能客服系统提示词内置安全红线（不索取/不泄露密码、银行卡、验证码等）
- RAG 数据查询已按 `memberId` 限定，不会跨用户泄露
- 生产环境请替换所有默认密码和密钥
