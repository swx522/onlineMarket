
package com.garfield.commands;

import com.garfield.model.Task;
import com.garfield.repository.GarfieldTaskRepository;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.util.List;

@Command(
    name = "list",
    description = "显示任务列表"
)
public class ListCommand implements Runnable {
    
    @Option(names = {"--status"}, 
            description = "按状态筛选 (OPEN/DONE)")
    private String statusStr;
    
    @Option(names = {"--priority"}, 
            description = "按优先级筛选 (LOW/MEDIUM/HIGH)")
    private String priorityStr;
    
    private GarfieldTaskRepository repository;
    
    public ListCommand(GarfieldTaskRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public void run() {
        try {
            List<Task> tasks;
            
            // 按状态筛选
            if (statusStr != null && !statusStr.trim().isEmpty()) {
                try {
                    Task.Status status = Task.Status.valueOf(statusStr.toUpperCase());
                    tasks = repository.findByStatus(status);
                } catch (IllegalArgumentException e) {
                    System.err.println("错误: 无效的状态 '" + statusStr + "'。请使用 OPEN 或 DONE");
                    return;
                }
            } else {
                tasks = repository.findAll();
            }
            
            // 按优先级筛选
            if (priorityStr != null && !priorityStr.trim().isEmpty()) {
                try {
                    Task.Priority priority = Task.Priority.valueOf(priorityStr.toUpperCase());
                    tasks = tasks.stream()
                            .filter(task -> task.getPriority() == priority)
                            .toList();
                } catch (IllegalArgumentException e) {
                    System.err.println("错误: 无效的优先级 '" + priorityStr + "'。请使用 LOW, MEDIUM 或 HIGH");
                    return;
                }
            }
            
            if (tasks.isEmpty()) {
                System.out.println("没有找到符合条件的任务");
                return;
            }
            
            System.out.println("任务列表 (" + tasks.size() + " 个任务):");
            System.out.println("=" + "=".repeat(60));
            
            for (int i = 0; i < tasks.size(); i++) {
                Task task = tasks.get(i);
                System.out.printf("%d. %s%n", i + 1, task.toString());
                
                // 如果是DeadlineTask，显示额外信息
                if (task instanceof com.garfield.model.DeadlineTask) {
                    com.garfield.model.DeadlineTask deadlineTask = (com.garfield.model.DeadlineTask) task;
                    System.out.printf("   截止时间: %s | 剩余时间: %s%n", 
                        deadlineTask.getDeadline().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                        deadlineTask.getTimeRemaining());
                }
            }
            
        } catch (Exception e) {
            System.err.println("意外错误: " + e.getMessage());
        }
    }
}
