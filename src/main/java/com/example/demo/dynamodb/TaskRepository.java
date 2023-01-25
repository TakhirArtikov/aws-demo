package com.example.demo.dynamodb;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TaskRepository extends CrudRepository<TaskEntity, String> {
    Optional<TaskEntity> findByTaskId(String taskId);
}
