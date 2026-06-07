package com.macro.mall.assistant.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 对话请求参数。
 */
@Data
@ApiModel("智能助手对话请求")
public class ChatRequest {

    @ApiModelProperty(value = "会话ID，用于关联上下文；不传则后端自动生成一个新会话", example = "sess-20240101-abc")
    private String sessionId;

    @ApiModelProperty(value = "用户标识，用于限流维度；可传会员ID，不传则按会话/IP限流", example = "member-1001")
    private String userId;

    @ApiModelProperty(value = "会员ID（数据库真实用户ID），传入后可查询该用户的订单、商品、账号等真实数据", example = "1")
    private Long memberId;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "消息内容过长，请控制在2000字以内")
    @ApiModelProperty(value = "用户输入的问题", required = true, example = "我买的商品什么时候发货？")
    private String message;
}
