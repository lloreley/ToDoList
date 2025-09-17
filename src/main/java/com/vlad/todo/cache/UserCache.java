package com.vlad.todo.cache;

import com.vlad.todo.dto.UserDtoResponse;
import org.springframework.stereotype.Component;

@Component
public class UserCache extends LfuCache<UserDtoResponse> {
    public UserCache() {
        super(3);
    }
}
