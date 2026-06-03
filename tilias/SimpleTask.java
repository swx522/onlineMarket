//李宇轩 2353737
package com.garfield.model;

/**
 * 简单任务类，基本的待办事项
 */
public class SimpleTask extends Task {
    
    public SimpleTask(String title, Priority priority) {
        super(title, priority);
    }
    
    @Override
    public String describe() {
        return String.format("简单任务: %s\n" +
                           "ID: %s\n" +
                           "优先级: %s\n" +
                           "状态: %s\n" +
                           "创建时间: %s",
            title, id, priority, status, createdAt);
    }
}
