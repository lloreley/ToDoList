package com.vlad.todo.controller;

import com.vlad.todo.dto.TaskDtoRequest;
import com.vlad.todo.dto.TaskDtoResponse;
import com.vlad.todo.exception.InvalidInputException;
import com.vlad.todo.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Задачи", description = "API для управления задачами у пользователей")
@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Получить все задачи", description =
                    "Возвращает список всех задач с возможностью фильтрации по статусу завершения")
    @GetMapping
    public ResponseEntity<List<TaskDtoResponse>> tasksByFilter(
            @Parameter(description =
                    "Статус завершения задач (true - завершенные, false - незавершенные)")
            @RequestParam(required = false) Boolean completed) {
        List<TaskDtoResponse> tasks = taskService.findAllTasks();
        if (completed != null) {
            tasks = tasks.stream()
                    .filter(task -> task.getIsCompleted() != null
                            && task.getIsCompleted() == completed)
                    .toList();
        }
        return ResponseEntity.ok(tasks);
    }

    @Operation(summary = "Получить задачи по пользователю",
            description = "Возвращает список задач для указанного пользователя по его ID")
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<TaskDtoResponse>> tasksByUser(
            @Parameter(description = "ID пользователя")
            @PathVariable long userId) {
        if (userId < 1) {
            throw new InvalidInputException("Id должен быть больше 0");
        }
        return ResponseEntity.ok(taskService.findTasksByUser(userId));
    }

    @Operation(summary = "Создать новую задачу",
            description = "Создает новую задачу и возвращает её данные")
    @PostMapping("/saveTask")
    public ResponseEntity<TaskDtoResponse> saveTask(
            @Parameter(description = "Данные новой задачи")
            @Valid @RequestBody TaskDtoRequest taskDto) {
        TaskDtoResponse savedTask = taskService.saveTask(taskDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }

    @Operation(summary = "Обновить задачу",
            description = "Обновляет задачу по её ID и возвращает обновленные данные")
    @PutMapping("/{id}")
    public ResponseEntity<TaskDtoResponse> updateTask(
            @Parameter(description = "ID задачи")
            @PathVariable long id,
            @Parameter(description = "Обновленные данные задачи")
            @RequestBody TaskDtoRequest taskDto) {
        if (id < 1) {
            throw new InvalidInputException("Id должен быть больше 0");
        }
        return ResponseEntity.ok(taskService.updateTask(id, taskDto));
    }

    @Operation(summary = "Получить задачу по ID",
            description = "Возвращает задачу по её уникальному идентификатору")
    @GetMapping("/{id}")
    public ResponseEntity<TaskDtoResponse> findTaskById(
            @Parameter(description = "ID задачи")
            @PathVariable long id) {
        if (id < 1) {
            throw new InvalidInputException("Id должен быть больше 0");
        }
        return ResponseEntity.ok(taskService.findTaskById(id));
    }

    @Operation(summary = "Удалить задачу",
            description = "Удаляет задачу по её ID")
    @DeleteMapping("/deleteTask/{id}")
    public ResponseEntity<Void> deleteTaskById(
            @Parameter(description = "ID задачи")
            @PathVariable long id) {
        if (id < 1) {
            throw new InvalidInputException("Id должен быть больше 0");
        }
        taskService.deleteTaskById(id);
        return ResponseEntity.ok().build();
    }
}