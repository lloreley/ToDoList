package com.vlad.todo.dto;

import lombok.Data;

@Data
public class UserDtoResponse {
    private long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
}
