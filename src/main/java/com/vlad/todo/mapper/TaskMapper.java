package com.vlad.todo.mapper;

import com.vlad.todo.dto.TaskDtoRequest;
import com.vlad.todo.dto.TaskDtoResponse;
import com.vlad.todo.model.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskDtoResponse toDto(Task task) {
        TaskDtoResponse taskDtoResponse = new TaskDtoResponse();
        taskDtoResponse.setId(task.getId());
        taskDtoResponse.setTitle(task.getTitle());
        taskDtoResponse.setContent(task.getContent());
        taskDtoResponse.setDeadlineDate(task.getDeadlineDate());
        taskDtoResponse.setIsImportant(task.getIsImportant());
        taskDtoResponse.setIsCompleted(task.getIsCompleted());
        taskDtoResponse.setUserId(task.getUser().getId());
        return taskDtoResponse;
    }

    public Task toEntity(TaskDtoRequest taskDtoRequest) {
        Task task = new Task();
        task.setTitle(taskDtoRequest.getTitle());
        task.setContent(taskDtoRequest.getContent());
        task.setDeadlineDate(taskDtoRequest.getDeadlineDate());
        task.setIsImportant(taskDtoRequest.getIsImportant());
        task.setIsCompleted(taskDtoRequest.getIsCompleted());
        return task;
    }

}
