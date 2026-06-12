
package com.garfield.repository;

import com.garfield.model.Task;
import com.garfield.exception.TaskException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 内存中的任务仓库实现
 */
public class InMemoryGarfieldTaskRepository implements GarfieldTaskRepository {
    
    private final Map<String, Task> tasks = new HashMap<>();
    
    @Override
    public void save(Task task) throws TaskException {
        if (task == null) {
            throw new TaskException("任务不能为空");
        }
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new TaskException("任务标题不能为空");
        }
        if (tasks.containsKey(task.getId())) {
            throw new TaskException("任务ID已存在: " + task.getId());
        }
        
        tasks.put(task.getId(), task);
    }
    
    @Override
    public Task findById(String id) throws TaskException {
        if (id == null || id.trim().isEmpty()) {
            throw new TaskException("任务ID不能为空");
        }
        
        Task task = tasks.get(id);
        if (task == null) {
            throw new TaskException("未找到ID为 " + id + " 的任务");
        }
        
        return task;
    }
    
    @Override
    public List<Task> findAll() {
        return new ArrayList<>(tasks.values());
    }
    
    @Override
    public void deleteById(String id) throws TaskException {
        if (id == null || id.trim().isEmpty()) {
            throw new TaskException("任务ID不能为空");
        }
        
        Task removed = tasks.remove(id);
        if (removed == null) {
            throw new TaskException("未找到ID为 " + id + " 的任务");
        }
    }
    
    @Override
    public List<Task> findByStatus(Task.Status status) {
        if (status == null) {
            return findAll();
        }
        
        return tasks.values().stream()
                .filter(task -> task.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    @Override
    public void update(Task task) throws TaskException {
        if (task == null) {
            throw new TaskException("任务不能为空");
        }
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new TaskException("任务标题不能为空");
        }
        
        if (!tasks.containsKey(task.getId())) {
            throw new TaskException("未找到ID为 " + task.getId() + " 的任务");
        }
        
        tasks.put(task.getId(), task);
    }
}
