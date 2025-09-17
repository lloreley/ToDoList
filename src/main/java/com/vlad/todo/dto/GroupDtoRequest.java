package com.vlad.todo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GroupDtoRequest {
    @Size(max = 50, message = "Длина названия слишком большая")
    @NotBlank(message = "Название не должно быть пустым")
    private String name;
    @Size(max = 200, message = "Длина описания слишком большая")
    @NotBlank(message = "Описание не должно быть пустым")
    private String description;
}

