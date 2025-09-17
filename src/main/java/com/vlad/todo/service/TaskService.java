package com.vlad.todo.service;

import static com.vlad.todo.service.UserService.USER_WITH_ID_NOT_FOUND;

import com.vlad.todo.dto.TaskDtoRequest;
import com.vlad.todo.dto.TaskDtoResponse;
import com.vlad.todo.exception.InvalidInputException;
import com.vlad.todo.exception.NotFoundException;
import com.vlad.todo.mapper.TaskMapper;
import com.vlad.todo.model.Task;
import com.vlad.todo.model.User;
import com.vlad.todo.repository.TaskRepository;
import com.vlad.todo.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Transactional
public class TaskService {
    public static final String TASK_WITH_ID_NOT_FOUND = "Задача с id %d не найдена";

    private final TaskMapper taskMapper;
    private TaskRepository taskRepository;
    private UserRepository userRepository;

    public List<TaskDtoResponse> findAllTasks() {
        List<TaskDtoResponse> tasksDtoResponse = new ArrayList<>();
        taskRepository.findAll().forEach(
                task -> tasksDtoResponse.add(taskMapper.toDto(task)));
        return tasksDtoResponse;
    }

    public List<TaskDtoResponse> findTasksByUser(long userId) {
        List<Task> tasks = taskRepository.findByUser(userId);
        List<TaskDtoResponse> tasksDtoResponse = new ArrayList<>();

        tasks.forEach(task -> tasksDtoResponse.add(taskMapper.toDto(task)));
        return tasksDtoResponse;
    }

    public TaskDtoResponse findTaskById(long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format(TASK_WITH_ID_NOT_FOUND, id)));
        return taskMapper.toDto(task);
    }

    public TaskDtoResponse saveTask(TaskDtoRequest taskDtoRequest) {
        if (taskDtoRequest.getUserId() < 1) {
            throw new InvalidInputException("Id пользователя должен быть больше 0");
        }
        User user = userRepository.findById(taskDtoRequest.getUserId())
                .orElseThrow(() -> new NotFoundException(
                        String.format(USER_WITH_ID_NOT_FOUND, taskDtoRequest.getUserId())));
        Task task = taskMapper.toEntity(taskDtoRequest);
        task.setUser(user);
        taskRepository.save(task);
        return taskMapper.toDto(task);
    }

    public TaskDtoResponse updateTask(long id, TaskDtoRequest taskDtoRequest) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format(TASK_WITH_ID_NOT_FOUND, id)));

        if (taskDtoRequest.getTitle() != null) {
            task.setTitle(taskDtoRequest.getTitle());
        }
        if (taskDtoRequest.getContent() != null) {
            task.setContent(taskDtoRequest.getContent());
        }
        if (taskDtoRequest.getIsCompleted() != null) {
            task.setIsCompleted(taskDtoRequest.getIsCompleted());
        }
        if (taskDtoRequest.getDeadlineDate() != null) {
            task.setDeadlineDate(taskDtoRequest.getDeadlineDate());
        }
        if (taskDtoRequest.getIsImportant() != null) {
            task.setIsImportant(taskDtoRequest.getIsImportant());
        }
        taskRepository.save(task);
        return taskMapper.toDto(task);
    }

    public void deleteTaskById(long id) {
        if (!taskRepository.existsById(id)) {
            throw new NotFoundException(
                String.format(TASK_WITH_ID_NOT_FOUND, id));
        }
        taskRepository.deleteById(id);
    }
}
