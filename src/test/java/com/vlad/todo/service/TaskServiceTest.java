package com.vlad.todo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.vlad.todo.dto.TaskDtoRequest;
import com.vlad.todo.dto.TaskDtoResponse;
import com.vlad.todo.exception.InvalidInputException;
import com.vlad.todo.exception.NotFoundException;
import com.vlad.todo.mapper.TaskMapper;
import com.vlad.todo.model.Task;
import com.vlad.todo.model.User;
import com.vlad.todo.repository.TaskRepository;
import com.vlad.todo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

class TaskServiceTest {

    @InjectMocks
    private TaskService taskService;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    private Task task;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        task = new Task();
        task.setId(1L);
        task.setTitle("New Title");
        task.setIsImportant(false);
        task.setIsCompleted(false);
        task.setContent("New Content");
        task.setDeadlineDate(LocalDate.now());
        task.setUser(user);
    }

    @Test
    void findAllTasks_ReturnsTaskDtoResponseList() {
        TaskDtoResponse taskDtoResponse = new TaskDtoResponse();
        when(taskRepository.findAll()).thenReturn(Collections.singletonList(task));
        when(taskMapper.toDto(task)).thenReturn(taskDtoResponse);

        var result = taskService.findAllTasks();

        assertEquals(1, result.size());
        assertSame(taskDtoResponse, result.get(0));
    }

    @Test
    void findTasksByUser_ReturnsTaskDtoResponseList() {
        TaskDtoResponse taskDtoResponse = new TaskDtoResponse();
        when(taskRepository.findByUser(1L)).thenReturn(Collections.singletonList(task));
        when(taskMapper.toDto(task)).thenReturn(taskDtoResponse);

        var result = taskService.findTasksByUser(1L);

        assertEquals(1, result.size());
        assertSame(taskDtoResponse, result.get(0));
    }

    @Test
    void findTaskById_ReturnsTaskDtoResponse() {
        TaskDtoResponse taskDtoResponse = new TaskDtoResponse();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(taskDtoResponse);

        var result = taskService.findTaskById(1L);

        assertSame(taskDtoResponse, result);
    }

    @Test
    void findTaskById_ThrowsNotFoundException_WhenTaskNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            taskService.findTaskById(1L);
        });

        assertEquals("Задача с id 1 не найдена", exception.getMessage());
    }

    @Test
    void saveTask_ShouldThrowInvalidInputException_WhenUserIdIsInvalid() {
        TaskDtoRequest taskDtoRequest = new TaskDtoRequest();
        taskDtoRequest.setUserId(0L);

        InvalidInputException exception = assertThrows(InvalidInputException.class, () -> {
            taskService.saveTask(taskDtoRequest);
        });

        assertEquals("Id пользователя должен быть больше 0", exception.getMessage());
    }

    @Test
    void saveTask_ShouldThrowNotFoundException_WhenUserNotFound() {
        TaskDtoRequest taskDtoRequest = new TaskDtoRequest();
        taskDtoRequest.setUserId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            taskService.saveTask(taskDtoRequest);
        });

        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
    }
    @Test
    void saveTask_ShouldReturnTaskDtoResponse_WhenTaskIsSavedSuccessfully() {
        TaskDtoRequest taskDtoRequest = new TaskDtoRequest();
        taskDtoRequest.setUserId(1L);
        TaskDtoResponse taskDtoResponse = new TaskDtoResponse();

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(taskMapper.toEntity(taskDtoRequest)).thenReturn(task);
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskDtoResponse);

        var result = taskService.saveTask(taskDtoRequest);
        assertNotNull(result);
        assertSame(taskDtoResponse, result);
        verify(taskRepository).save(task);
    }

    @Test
    void updateTask_ReturnsUpdatedTaskDtoResponse() {
        TaskDtoRequest taskDtoRequest = new TaskDtoRequest();
        taskDtoRequest.setTitle("New Title");
        taskDtoRequest.setIsImportant(false);
        taskDtoRequest.setIsCompleted(false);
        taskDtoRequest.setContent("New Content");
        taskDtoRequest.setDeadlineDate(LocalDate.now());
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(new TaskDtoResponse());

        var result = taskService.updateTask(1L, taskDtoRequest);

        assertNotNull(result);
        assertEquals("New Title", task.getTitle());
    }

    @Test
    void updateTask_butTaskNotProvided_ReturnsUpdatedTaskDtoResponse() {
        TaskDtoRequest taskDtoRequest = new TaskDtoRequest();
        taskDtoRequest.setTitle(null);
        taskDtoRequest.setContent(null);
        taskDtoRequest.setIsCompleted(null);
        taskDtoRequest.setIsCompleted(null);
        taskDtoRequest.setDeadlineDate(null);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(new TaskDtoResponse());

        var result = taskService.updateTask(1L, taskDtoRequest);

        assertNotNull(result);
        assertEquals("New Title", task.getTitle());
    }

    @Test
    void updateTask_ThrowsNotFoundException_WhenTaskNotFound() {
        TaskDtoRequest taskDtoRequest = new TaskDtoRequest();
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            taskService.updateTask(1L, taskDtoRequest);
        });

        assertEquals("Задача с id 1 не найдена", exception.getMessage());
    }

    @Test
    void deleteTaskById_Success() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> {
            taskService.deleteTaskById(1L);
        });

        verify(taskRepository).deleteById(1L);
    }

    @Test
    void deleteTaskById_ThrowsNotFoundException_WhenTaskNotFound() {
        when(taskRepository.existsById(1L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            taskService.deleteTaskById(1L);
        });

        assertEquals("Задача с id 1 не найдена", exception.getMessage());
    }
}