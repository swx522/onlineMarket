package com.macro.mall.assistant.controller;

import com.macro.mall.assistant.api.CommonResult;
import com.macro.mall.assistant.dto.ChatRequest;
import com.macro.mall.assistant.dto.ChatResponse;
import com.macro.mall.assistant.service.AssistantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 智能客服助手对话接口。
 */
@RestController
@Api(tags = "AssistantController", value = "智能客服助手")
@RequestMapping("/assistant")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @ApiOperation("发起对话：向智能助手提问并获取回复")
    @PostMapping("/chat")
    public CommonResult<ChatResponse> chat(@Valid @RequestBody ChatRequest request,
                                           HttpServletRequest httpRequest) {
        ChatResponse response = assistantService.chat(request, clientIp(httpRequest));
        return CommonResult.success(response);
    }

    @ApiOperation("清空指定会话的对话历史")
    @DeleteMapping("/history/{sessionId}")
    public CommonResult<Void> clearHistory(@PathVariable String sessionId) {
        assistantService.clearHistory(sessionId);
        return CommonResult.success(null, "会话已清空");
    }

    @ApiOperation("健康检查")
    @GetMapping("/health")
    public CommonResult<String> health() {
        return CommonResult.success("ok");
    }

    /**
     * 解析真实客户端 IP（兼容常见反向代理头），作为未传 userId 时的限流维度。
     */
    private String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            int idx = ip.indexOf(',');
            return idx > 0 ? ip.substring(0, idx).trim() : ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }
}
