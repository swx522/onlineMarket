//李宇轩 2353737
package com.garfield.commands;

import com.garfield.repository.GarfieldTaskRepository;
import com.garfield.service.TaskExporter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import java.io.IOException;
import java.util.List;

@Command(
    name = "export",
    description = "导出任务到JSON文件"
)
public class ExportCommand implements Runnable {
    
    @Option(names = {"--file"}, 
            description = "输出文件名", 
            required = true)
    private String filename;
    
    private GarfieldTaskRepository repository;
    private TaskExporter taskExporter;
    
    public ExportCommand(GarfieldTaskRepository repository, TaskExporter taskExporter) {
        this.repository = repository;
        this.taskExporter = taskExporter;
    }
    
    @Override
    public void run() {
        try {
            List<com.garfield.model.Task> tasks = repository.findAll();
            
            if (tasks.isEmpty()) {
                System.out.println("没有任务可以导出");
                return;
            }
            
            taskExporter.exportTasks(tasks, filename);
            
        } catch (IOException e) {
            System.err.println("导出文件时发生错误: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("意外错误: " + e.getMessage());
        }
    }
}
