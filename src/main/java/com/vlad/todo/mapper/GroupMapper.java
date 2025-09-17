package com.vlad.todo.mapper;

import com.vlad.todo.dto.*;
import com.vlad.todo.model.Group;
import com.vlad.todo.model.User;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GroupMapper {
    private final UserMapper userMapper;

    public Group toEntity(GroupDtoRequest groupDtoRequest) {
        Group group = new Group();
        group.setName(groupDtoRequest.getName());
        group.setDescription(groupDtoRequest.getDescription());
        return group;
    }

    public GroupDtoResponse toDto(Group group) {
        GroupDtoResponse groupDtoResponse = new GroupDtoResponse();
        groupDtoResponse.setName(group.getName());
        groupDtoResponse.setDescription(group.getDescription());
        groupDtoResponse.setId(group.getId());

        List<UserDtoResponse> usersDtoResponse = new ArrayList<>();
        for (User user : group.getUsers()) {
            usersDtoResponse.add(userMapper.toDto(user));
        }
        groupDtoResponse.setUsers(usersDtoResponse);
        return groupDtoResponse;
    }
}
