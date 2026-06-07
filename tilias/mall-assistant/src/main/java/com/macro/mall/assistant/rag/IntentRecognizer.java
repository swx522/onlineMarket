package com.macro.mall.assistant.rag;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于关键词规则的意图识别器。
 *
 * <p>对用户输入做快速分类，决定是否需要查询真实数据库以及查什么。
 * 规则简单但可控，不需要额外调一次 LLM，延迟极低。
 */
@Component
public class IntentRecognizer {

    /**
     * 意图类型
     */
    @Getter
    public enum IntentType {
        /** 订单相关：查物流、发货状态、订单列表等 */
        ORDER_QUERY,
        /** 商品相关：搜商品、问价格、查库存 */
        PRODUCT_SEARCH,
        /** 账号相关：积分、余额、会员等级 */
        ACCOUNT_QUERY,
        /** 优惠券相关 */
        COUPON_QUERY,
        /** 普通闲聊，不需要查数据库 */
        GENERAL_CHAT
    }

    /**
     * 订单关键词
     */
    private static final String[] ORDER_KEYWORDS = {
            "订单", "发货", "物流", "快递", "退款", "退货", "售后",
            "买了", "购买", "下单", "配送", "签收", "收货", "运单",
            "催单", "改地址", "修改地址", "取消订单"
    };
    private static final Pattern ORDER_SN_PATTERN = Pattern.compile("\\d{16,20}");

    /**
     * 商品关键词
     */
    private static final String[] PRODUCT_KEYWORDS = {
            "有卖", "有没有", "有货", "商品", "价格", "多少钱",
            "什么牌子", "推荐", "哪个好", "有什么", "规格", "参数",
            "库存", "颜色", "尺码", "型号", "手机", "电脑", "耳机",
            "衣服", "鞋子", "家电", "食品", "划算", "打折", "新品"
    };

    /**
     * 账号关键词
     */
    private static final String[] ACCOUNT_KEYWORDS = {
            "积分", "余额", "会员", "等级", "成长值", "个人信息",
            "我的账户", "账号", "绑定", "昵称", "手机号", "收货地址"
    };

    /**
     * 优惠券关键词
     */
    private static final String[] COUPON_KEYWORDS = {
            "优惠券", "满减", "折扣券", "代金券", "领券", "卡券", "红包"
    };

    /**
     * 识别用户消息的意图。
     */
    public IntentResult recognize(String message) {
        if (!StringUtils.hasText(message)) {
            return new IntentResult(IntentType.GENERAL_CHAT, null);
        }
        String text = message.toLowerCase();

        // 按优先级匹配：订单 > 商品 > 优惠券 > 账号 > 闲聊

        // 1. 订单意图：包含订单关键词 或 包含纯数字订单号
        for (String kw : ORDER_KEYWORDS) {
            if (text.contains(kw)) {
                String orderSn = extractOrderSn(text);
                return new IntentResult(IntentType.ORDER_QUERY, orderSn);
            }
        }
        Matcher m = ORDER_SN_PATTERN.matcher(text);
        if (m.find()) {
            return new IntentResult(IntentType.ORDER_QUERY, m.group());
        }

        // 2. 商品意图
        for (String kw : PRODUCT_KEYWORDS) {
            if (text.contains(kw)) {
                return new IntentResult(IntentType.PRODUCT_SEARCH, extractKeyword(text, kw));
            }
        }

        // 3. 优惠券意图
        for (String kw : COUPON_KEYWORDS) {
            if (text.contains(kw)) {
                return new IntentResult(IntentType.COUPON_QUERY, null);
            }
        }

        // 4. 账号意图
        for (String kw : ACCOUNT_KEYWORDS) {
            if (text.contains(kw)) {
                return new IntentResult(IntentType.ACCOUNT_QUERY, null);
            }
        }

        return new IntentResult(IntentType.GENERAL_CHAT, null);
    }

    /**
     * 从文本中提取订单号（16-20 位连续数字）
     */
    private String extractOrderSn(String text) {
        Matcher m = ORDER_SN_PATTERN.matcher(text);
        return m.find() ? m.group() : null;
    }

    /**
     * 从消息中提取搜索关键词（触发词后的核心内容）
     */
    private String extractKeyword(String text, String triggerWord) {
        int idx = text.indexOf(triggerWord);
        if (idx < 0) return null;
        String after = text.substring(idx + triggerWord.length()).trim();
        // 去掉常见疑问词
        after = after.replaceAll("[?？!！。，,]", " ").trim();
        return StringUtils.hasText(after) ? after : triggerWord;
    }

    /**
     * 意图识别结果
     */
    @Getter
    public static class IntentResult {
        private final IntentType intentType;
        /** 附加参数：订单号 或 搜索关键词 */
        private final String param;

        public IntentResult(IntentType intentType, String param) {
            this.intentType = intentType;
            this.param = param;
        }
    }
}
