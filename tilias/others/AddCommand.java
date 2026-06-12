
package com.garfield.commands;

import com.garfield.model.*;
import com.garfield.repository.GarfieldTaskRepository;
import com.garfield.exception.TaskException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Command(
    name = "add",
    description = "添加新任务"
)
public class AddCommand implements Runnable {
    
    @Option(names = {"--title"}, 
            description = "任务标题", 
            required = true)
    private String title;
    
    @Option(names = {"--priority"}, 
            description = "任务优先级 (LOW/MEDIUM/HIGH)", 
            defaultValue = "MEDIUM")
    private String priorityStr;
    
    @Option(names = {"--deadline"}, 
            description = "截止时间 (格式: yyyy-MM-dd HH:mm)")
    private String deadlineStr;
    
    private GarfieldTaskRepository repository;
    
    public AddCommand(GarfieldTaskRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public void run() {
        try {
            // 验证标题
            if (title == null || title.trim().isEmpty()) {
                System.err.println("错误: 任务标题不能为空");
                return;
            }
            
            // 解析优先级
            Task.Priority priority;
            try {
                priority = Task.Priority.valueOf(priorityStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("错误: 无效的优先级 '" + priorityStr + "'。请使用 LOW, MEDIUM 或 HIGH");
                return;
            }
            
            Task task;
            
            // 如果有截止时间，创建DeadlineTask
            if (deadlineStr != null && !deadlineStr.trim().isEmpty()) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    LocalDateTime deadline = LocalDateTime.parse(deadlineStr, formatter);
                    task = new DeadlineTask(title, priority, deadline);
                } catch (DateTimeParseException e) {
                    System.err.println("错误: 无效的截止时间格式。请使用格式: yyyy-MM-dd HH:mm");
                    return;
                }
            } else {
                // 否则创建SimpleTask
                task = new SimpleTask(title, priority);
            }
            
            repository.save(task);
            System.out.println("任务添加成功!");
            System.out.println("Task" + task.toString());
            
        } catch (TaskException e) {
            System.err.println("错误: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("意外错误: " + e.getMessage());
        }
    }
}
