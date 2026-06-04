package com.macro.mall.assistant.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话返回结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("智能助手对话响应")
public class ChatResponse {

    @ApiModelProperty("会话ID，请在下一轮对话中带上以维持上下文")
    private String sessionId;

    @ApiModelProperty("助手回复内容")
    private String reply;

    @ApiModelProperty("本次回复使用的模型名称")
    private String model;

    @ApiModelProperty("是否为降级回复（true 表示大模型不可用，返回的是兜底话术）")
    private boolean fallback;

    @ApiModelProperty("回复时间戳（毫秒）")
    private Long timestamp;
}
