package com.vlad.todo.dto;

import java.util.List;
import lombok.Data;

@Data
public class GroupDtoResponse {
    private Long id;
    private String name;
    private String description;
    private List<UserDtoResponse> users;
}
