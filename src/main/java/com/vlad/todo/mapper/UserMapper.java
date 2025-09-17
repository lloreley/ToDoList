package com.vlad.todo.mapper;

import com.vlad.todo.dto.TaskDtoRequest;
import com.vlad.todo.dto.TaskDtoResponse;
import com.vlad.todo.dto.UserDtoRequest;
import com.vlad.todo.dto.UserDtoResponse;
import com.vlad.todo.model.Task;
import com.vlad.todo.model.User;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserMapper {
    private final TaskMapper taskMapper;

    public User toEntity(UserDtoRequest userDtoRequest) {
        User user = new User();
        user.setFirstName(userDtoRequest.getFirstName());
        user.setLastName(userDtoRequest.getLastName());
        user.setEmail(userDtoRequest.getEmail());
        user.setPhone(userDtoRequest.getPhone());
        return user;
    }

    public UserDtoResponse toDto(User user) {
        UserDtoResponse userDtoResponse = new UserDtoResponse();
        userDtoResponse.setId(user.getId());
        userDtoResponse.setEmail(user.getEmail());
        userDtoResponse.setFirstName(user.getFirstName());
        userDtoResponse.setLastName(user.getLastName());
        userDtoResponse.setPhone(user.getPhone());
        return userDtoResponse;
    }
}
