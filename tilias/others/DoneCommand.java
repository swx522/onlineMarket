
package com.garfield.commands;

import com.garfield.model.Task;
import com.garfield.repository.GarfieldTaskRepository;
import com.garfield.exception.TaskException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "done",
    description = "标记任务为已完成"
)
public class DoneCommand implements Runnable {
    
    @Option(names = {"--id"}, 
            description = "任务ID", 
            required = true)
    private String id;
    
    private GarfieldTaskRepository repository;
    
    public DoneCommand(GarfieldTaskRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public void run() {
        try {
            Task task = repository.findById(id);
            task.setStatus(Task.Status.DONE);
            repository.update(task);
            
            System.out.println("任务标记为已完成!");
            System.out.println("Task" + task.toString());
            
        } catch (TaskException e) {
            System.err.println("错误: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("意外错误: " + e.getMessage());
        }
    }
}
