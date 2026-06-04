package com.macro.mall.assistant.service;

import cn.hutool.dfa.WordTree;
import com.macro.mall.assistant.config.AssistantProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 敏感词过滤器。
 *
 * <p>基于 Hutool 的 DFA（确定有限自动机）{@link WordTree} 实现，支持快速匹配与脱敏替换。
 * 内置一份基础词库，并可通过 {@code assistant.sensitive.words} 追加自定义词。
 *
 * <p>注意：内置词库仅作演示，生产环境应接入公司统一的内容安全服务。
 */
@Component
public class SensitiveWordFilter {

    private final AssistantProperties properties;
    private final WordTree wordTree = new WordTree();

    /** 基础敏感词（演示用，可按业务扩充或替换为外部词库） */
    private static final List<String> DEFAULT_WORDS = Arrays.asList(
            "暴力", "色情", "赌博", "毒品", "诈骗", "枪支", "政治敏感"
    );

    public SensitiveWordFilter(AssistantProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        List<String> words = new ArrayList<>(DEFAULT_WORDS);
        if (properties.getSensitive().getWords() != null) {
            words.addAll(properties.getSensitive().getWords());
        }
        words.stream().filter(StringUtils::hasText).forEach(wordTree::addWord);
    }

    /**
     * 是否包含敏感词。
     */
    public boolean contains(String text) {
        if (!properties.getSensitive().isEnabled() || !StringUtils.hasText(text)) {
            return false;
        }
        return wordTree.isMatch(text);
    }

    /**
     * 将文本中的敏感词替换为 *（用于回复内容的兜底脱敏）。
     */
    public String filter(String text) {
        if (!properties.getSensitive().isEnabled() || !StringUtils.hasText(text)) {
            return text;
        }
        String result = text;
        for (String word : wordTree.matchAll(text)) {
            if (StringUtils.hasText(word)) {
                result = result.replace(word, repeat("*", word.length()));
            }
        }
        return result;
    }

    private static String repeat(String s, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(s);
        }
        return sb.toString();
    }
}
