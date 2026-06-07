package com.macro.mall.assistant.rag;

import com.macro.mall.mapper.*;
import com.macro.mall.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 真实数据库查询服务 —— RAG 的数据检索层。
 *
 * <p>根据 {@link IntentRecognizer.IntentResult} 查询 MySQL 中的真实业务数据，
 * 并将结果格式化为易于 LLM 理解的 Markdown 文本，注入到 system prompt 中。
 *
 * <p>安全约定：所有查询均已限定 {@code memberId}，不会跨用户泄露数据。
 */
@Slf4j
@Service
public class DataQueryService {

    private final OmsOrderMapper orderMapper;
    private final OmsOrderItemMapper orderItemMapper;
    private final PmsProductMapper productMapper;
    private final UmsMemberMapper memberMapper;
    private final SmsCouponHistoryMapper couponHistoryMapper;
    private final SmsCouponMapper couponMapper;

    private static final Map<Integer, String> ORDER_STATUS_MAP = new LinkedHashMap<>();

    static {
        ORDER_STATUS_MAP.put(0, "待付款");
        ORDER_STATUS_MAP.put(1, "待发货");
        ORDER_STATUS_MAP.put(2, "已发货");
        ORDER_STATUS_MAP.put(3, "已完成");
        ORDER_STATUS_MAP.put(4, "已关闭");
        ORDER_STATUS_MAP.put(5, "无效订单");
    }

    public DataQueryService(OmsOrderMapper orderMapper,
                            OmsOrderItemMapper orderItemMapper,
                            PmsProductMapper productMapper,
                            UmsMemberMapper memberMapper,
                            SmsCouponHistoryMapper couponHistoryMapper,
                            SmsCouponMapper couponMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productMapper = productMapper;
        this.memberMapper = memberMapper;
        this.couponHistoryMapper = couponHistoryMapper;
        this.couponMapper = couponMapper;
    }

    /**
     * 执行数据查询，返回可直接拼入 prompt 的格式化文本；无数据返回 null。
     */
    public String query(IntentRecognizer.IntentResult intent, Long memberId) {
        if (memberId == null) {
            return null;
        }
        try {
            switch (intent.getIntentType()) {
                case ORDER_QUERY:
                    return queryOrders(memberId, intent.getParam());
                case PRODUCT_SEARCH:
                    return searchProducts(intent.getParam());
                case ACCOUNT_QUERY:
                    return queryAccount(memberId);
                case COUPON_QUERY:
                    return queryCoupons(memberId);
                default:
                    return null;
            }
        } catch (Exception e) {
            log.warn("查询数据库失败, intent={}, memberId={}, msg={}",
                    intent.getIntentType(), memberId, e.getMessage());
            return null;
        }
    }

    // ======================== 订单查询 ========================

    private String queryOrders(Long memberId, String orderSn) {
        OmsOrderExample example = new OmsOrderExample();
        OmsOrderExample.Criteria criteria = example.createCriteria()
                .andMemberIdEqualTo(memberId)
                .andDeleteStatusEqualTo(0);
        if (StringUtils.hasText(orderSn)) {
            // 尝试按订单号精确查
            criteria.andOrderSnLike("%" + orderSn + "%");
        }
        example.setOrderByClause("create_time desc");

        List<OmsOrder> orders = orderMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(orders)) {
            // 限定条数，避免 prompt 过长
            int max = Math.min(orders.size(), 5);
            orders = orders.subList(0, max);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("## 用户订单信息（共 ").append(orders.size()).append(" 条）\n");

        for (int i = 0; i < orders.size(); i++) {
            OmsOrder o = orders.get(i);
            String status = ORDER_STATUS_MAP.getOrDefault(o.getStatus(), "未知状态");
            sb.append(i + 1).append(". 订单号：").append(o.getOrderSn())
                    .append("，状态：").append(status)
                    .append("，金额：" + currency(o.getPayAmount()) + "元");
            if (StringUtils.hasText(o.getDeliveryCompany())) {
                sb.append("，物流：").append(o.getDeliveryCompany())
                        .append("（运单号：").append(o.getDeliverySn()).append("）");
            }
            if (o.getCreateTime() != null) {
                sb.append("，下单时间：").append(o.getCreateTime());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // ======================== 商品搜索 ========================

    private String searchProducts(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        PmsProductExample example = new PmsProductExample();
        example.createCriteria()
                .andDeleteStatusEqualTo(0)
                .andPublishStatusEqualTo(1)
                .andNameLike("%" + keyword + "%");

        List<PmsProduct> products = productMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(products)) {
            // 如果商品名没匹配到，尝试按 keywords 字段模糊匹配
            example.clear();
            example.createCriteria()
                    .andDeleteStatusEqualTo(0)
                    .andPublishStatusEqualTo(1)
                    .andKeywordsLike("%" + keyword + "%");
            products = productMapper.selectByExample(example);
        }

        if (CollectionUtils.isEmpty(products)) {
            return null;
        }
        int max = Math.min(products.size(), 5);
        products = products.subList(0, max);

        StringBuilder sb = new StringBuilder();
        sb.append("## 相关商品信息（共 ").append(products.size()).append(" 件）\n");
        for (int i = 0; i < products.size(); i++) {
            PmsProduct p = products.get(i);
            sb.append(i + 1).append(". ").append(p.getName())
                    .append("，价格：" + currency(p.getPrice()) + "元");
            if (p.getStock() != null) {
                sb.append("，库存：" + p.getStock()).append(p.getUnit() != null ? p.getUnit() : "件");
            }
            if (StringUtils.hasText(p.getSubTitle())) {
                sb.append("，简介：").append(p.getSubTitle());
            }
            if (StringUtils.hasText(p.getBrandName())) {
                sb.append("，品牌：").append(p.getBrandName());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // ======================== 账号查询 ========================

    private String queryAccount(Long memberId) {
        UmsMember member = memberMapper.selectByPrimaryKey(memberId);
        if (member == null) {
            return null;
        }
        return "## 用户账号信息\n" +
                "- 昵称：" + member.getNickname() + "\n" +
                "- 当前积分：" + (member.getIntegration() != null ? member.getIntegration() : 0) + "\n" +
                "- 成长值：" + (member.getGrowth() != null ? member.getGrowth() : 0) + "\n" +
                "- 会员等级ID：" + (member.getMemberLevelId() != null ? member.getMemberLevelId() : 0) + "\n";
    }

    // ======================== 优惠券查询 ========================

    private String queryCoupons(Long memberId) {
        SmsCouponHistoryExample historyExample = new SmsCouponHistoryExample();
        historyExample.createCriteria()
                .andMemberIdEqualTo(memberId)
                .andUseStatusEqualTo(0);  // 0 = 未使用
        List<SmsCouponHistory> histories = couponHistoryMapper.selectByExample(historyExample);

        if (CollectionUtils.isEmpty(histories)) {
            return null;
        }
        int max = Math.min(histories.size(), 5);
        histories = histories.subList(0, max);

        StringBuilder sb = new StringBuilder();
        sb.append("## 用户可用优惠券（共 ").append(histories.size()).append(" 张）\n");
        for (int i = 0; i < histories.size(); i++) {
            SmsCouponHistory h = histories.get(i);
            SmsCoupon coupon = couponMapper.selectByPrimaryKey(h.getCouponId());
            String name = coupon != null ? coupon.getName() : "优惠券#" + h.getCouponId();
            String amount = coupon != null ? currency(coupon.getAmount()) + "元" : "未知";
            sb.append(i + 1).append(". ").append(name)
                    .append("，面额：" + amount);
            if (coupon != null && coupon.getStartTime() != null) {
                sb.append("，有效期：" + cn.hutool.core.date.DateUtil.formatDate(coupon.getStartTime()))
                        .append("~" + cn.hutool.core.date.DateUtil.formatDate(coupon.getEndTime()));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 格式化金额，避免 BigDecimal 直接 toString 的冗长输出
     */
    private static String currency(java.math.BigDecimal value) {
        if (value == null) return "0.00";
        return value.setScale(2, java.math.RoundingMode.HALF_UP).toString();
    }
}
