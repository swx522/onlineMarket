package com.macro.mall.assistant.support;

import com.macro.mall.assistant.config.AssistantProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 系统提示词（System Prompt）构建器。
 *
 * <p>定义了助手的「电商客服」人设与边界，是控制回答质量与安全的核心。
 * 可通过 {@code assistant.llm.system-prompt} 覆盖默认人设。
 */
@Component
public class PromptBuilder {

    private final AssistantProperties properties;

    public PromptBuilder(AssistantProperties properties) {
        this.properties = properties;
    }

    private static final String DEFAULT_SYSTEM_PROMPT =
            "你是「Mall商城」的在线智能客服助手，名字叫小卖。请遵守以下规则：\n" +
            "1. 角色定位：友好、专业、简洁的电商客服，主要解答商品（价格、库存、规格、详情）、" +
            "订单（下单、支付、物流、状态）、售后（退换货、退款、发票）以及优惠活动等问题。\n" +
            "2. 回答要求：用中文回答，语气亲切；条理清晰，必要时分点说明；单次回复尽量控制在200字以内。\n" +
            "3. 能力边界：如果用户的问题超出电商客服范围（如闲聊、政治、医疗、法律等），" +
            "请礼貌地引导回购物相关话题。\n" +
            "4. 诚实原则：当你无法确定具体的订单或商品数据时，不要编造，应告知用户可在「我的订单」中查看，" +
            "或转接人工客服处理。\n" +
            "5. 安全红线：绝不索取或泄露用户的密码、支付密码、银行卡号、短信验证码等敏感信息；" +
            "不输出任何违法、违规或不当内容。\n" +
            "6. 兜底话术：遇到无法处理的诉求，引导用户联系人工客服（工作时间 9:00-21:00）。";

    /**
     * 获取系统提示词。
     */
    public String systemPrompt() {
        String custom = properties.getLlm().getSystemPrompt();
        return StringUtils.hasText(custom) ? custom : DEFAULT_SYSTEM_PROMPT;
    }

    /**
     * 构建包含真实业务数据的增强系统提示词。
     *
     * @param dataContext DataQueryService 查询到的真实数据文本；为 null 则退化为普通 system prompt
     */
    public String systemPromptWithData(String dataContext) {
        String base = systemPrompt();
        if (!StringUtils.hasText(dataContext)) {
            return base;
        }
        return base +
                "\n\n---\n" +
                "【重要】以下是你平台中该用户的<b>真实数据</b>，请直接引用这些数据来回答用户问题，" +
                "不要编造数据。这些数据只有你能看到，不要在回复中主动提及'系统的数据'或'数据库里显示'等措辞，" +
                "而应自然地当作已知信息来回答：\n\n" +
                dataContext;
    }
}
