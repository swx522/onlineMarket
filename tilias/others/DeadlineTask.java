
package com.garfield.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DeadlineTask extends Task {
    
    private LocalDateTime deadline;
    
    public DeadlineTask(String title, Priority priority, LocalDateTime deadline) {
        super(title, priority);
        this.deadline = deadline;
    }
    
    public LocalDateTime getDeadline() {
        return deadline;
    }
    
    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }
    
    /**
     * 获取剩余时间或显示"已过期"
     */
    public String getTimeRemaining() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(deadline)) {
            return "已过期";
        }
        
        long days = ChronoUnit.DAYS.between(now, deadline);
        long hours = ChronoUnit.HOURS.between(now, deadline) % 24;
        long minutes = ChronoUnit.MINUTES.between(now, deadline) % 60;
        
        if (days > 0) {
            return String.format("%d天 %d小时 %d分钟", days, hours, minutes);
        } else if (hours > 0) {
            return String.format("%d小时 %d分钟", hours, minutes);
        } else {
            return String.format("%d分钟", minutes);
        }
    }
    
    @Override
    public String describe() {
        String timeInfo = getTimeRemaining();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        return String.format("截止任务: %s\n" +
                           "ID: %s\n" +
                           "优先级: %s\n" +
                           "状态: %s\n" +
                           "创建时间: %s\n" +
                           "截止时间: %s\n" +
                           "剩余时间: %s",
            title, id, priority, status, createdAt.format(formatter), 
            deadline.format(formatter), timeInfo);
    }
}
