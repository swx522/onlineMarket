
package com.garfield.repository;

import com.garfield.model.Task;
import com.garfield.exception.TaskException;
import java.util.List;

/**
 * 任务仓库接口，定义标准的CRUD操作
 */
public interface GarfieldTaskRepository {
    
    /**
     * 保存任务
     */
    void save(Task task) throws TaskException;
    
    /**
     * 根据ID查找任务
     */
    Task findById(String id) throws TaskException;
    
    /**
     * 查找所有任务
     */
    List<Task> findAll();
    
    /**
     * 根据ID删除任务
     */
    void deleteById(String id) throws TaskException;
    
    /**
     * 根据状态查找任务
     */
    List<Task> findByStatus(Task.Status status);
    
    /**
     * 更新任务
     */
    void update(Task task) throws TaskException;
}
