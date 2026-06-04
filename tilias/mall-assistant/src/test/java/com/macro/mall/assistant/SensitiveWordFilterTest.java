package com.macro.mall.assistant;

import com.macro.mall.assistant.config.AssistantProperties;
import com.macro.mall.assistant.service.SensitiveWordFilter;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 敏感词过滤单元测试（纯逻辑，手动调用 init 模拟 @PostConstruct）。
 */
class SensitiveWordFilterTest {

    private SensitiveWordFilter newFilter(boolean enabled) {
        AssistantProperties props = new AssistantProperties();
        props.getSensitive().setEnabled(enabled);
        props.getSensitive().setWords(Collections.singletonList("自定义违禁词"));
        SensitiveWordFilter filter = new SensitiveWordFilter(props);
        filter.init();
        return filter;
    }

    @Test
    void shouldDetectBuiltInWord() {
        SensitiveWordFilter filter = newFilter(true);
        assertTrue(filter.contains("这是一段包含赌博的内容"));
    }

    @Test
    void shouldDetectCustomWord() {
        SensitiveWordFilter filter = newFilter(true);
        assertTrue(filter.contains("出现了自定义违禁词哦"));
    }

    @Test
    void shouldPassNormalText() {
        SensitiveWordFilter filter = newFilter(true);
        assertFalse(filter.contains("请问这款手机有货吗？什么时候发货？"));
    }

    @Test
    void shouldMaskSensitiveWordOnFilter() {
        SensitiveWordFilter filter = newFilter(true);
        String masked = filter.filter("包含赌博二字");
        assertFalse(masked.contains("赌博"));
        // 长度保持不变
        assertEquals("包含**二字".length(), masked.length());
    }

    @Test
    void disabledFilterShouldNotIntercept() {
        SensitiveWordFilter filter = newFilter(false);
        assertFalse(filter.contains("赌博"));
        assertEquals("赌博", filter.filter("赌博"));
    }
}
