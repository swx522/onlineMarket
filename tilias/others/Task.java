
package com.garfield.model;

import java.time.LocalDateTime;

/**
 * 抽象任务类，包含所有任务的基本属性
 */
public abstract class Task {
    
    public enum Status {
        OPEN, DONE
    }
    
    public enum Priority {
        LOW, MEDIUM, HIGH
    }
    
    protected String id;
    protected String title;
    protected LocalDateTime createdAt;
    protected Status status;
    protected Priority priority;
    
    // 静态计数器，用于生成十进制ID
    private static int taskCounter = 1000;
    
    public Task(String title, Priority priority) {
        this.id = "T-" + (++taskCounter);
        this.title = title;
        this.createdAt = LocalDateTime.now();
        this.status = Status.OPEN;
        this.priority = priority;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    /**
     * 抽象方法，每个子类必须实现以展示任务详情
     */
    public abstract String describe();
    
    @Override
    public String toString() {
        return String.format("[%s] %s (%s) - %s", 
            id, title, priority, status);
    }
}
