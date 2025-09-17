package com.vlad.todo.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.Data;

@Data
public class TaskDtoRequest {
    @Size(max = 50, message = "Длина заголовка слишком большая")
    @NotBlank(message = "Заголовок не должен быть пустым!")
    private String title;
    private String content;

    @NotNull(message = "Статус выполнения должен быть указан")
    private Boolean isCompleted;

    @Future(message = "Дедлайн не может быть в прошлом")
    @NotNull(message = "Дата дедлайна должна быть указана")
    private LocalDate deadlineDate;

    @NotNull(message = "Важность задачи должна быть указана")
    private Boolean isImportant;

    @Min(value = 1, message = "Id не может быть меньше 1")
    @NotNull(message = "Id пользователя должен быть указан")
    private Long userId;
}
