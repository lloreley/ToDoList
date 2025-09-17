package com.vlad.todo.service;

import com.vlad.todo.dto.GroupDtoRequest;
import com.vlad.todo.dto.GroupDtoResponse;
import com.vlad.todo.exception.NotFoundException;
import com.vlad.todo.mapper.GroupMapper;
import com.vlad.todo.model.Group;
import com.vlad.todo.repository.GroupRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Transactional
public class GroupService {
    public static final String GROUP_WITH_ID_NOT_FOUND = "Группа с id %d не найдена";

    private final GroupMapper groupMapper;
    private GroupRepository groupRepository;

    public List<GroupDtoResponse> findAll() {
        List<GroupDtoResponse> groupsDtoResponse = new ArrayList<>();
        groupRepository.findAll().forEach(
                group -> groupsDtoResponse.add(groupMapper.toDto(group)));
        return groupsDtoResponse;
    }

    public GroupDtoResponse findById(long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format(GROUP_WITH_ID_NOT_FOUND, id)));
        return groupMapper.toDto(group);
    }

    public GroupDtoResponse save(GroupDtoRequest groupDtoRequest) {
        Group group = groupMapper.toEntity(groupDtoRequest);
        groupRepository.save(group);
        return groupMapper.toDto(group);
    }

    public GroupDtoResponse update(long id, GroupDtoRequest groupDtoRequest) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format(GROUP_WITH_ID_NOT_FOUND, id)));

        if (groupDtoRequest.getName() != null) {
            group.setName(groupDtoRequest.getName());
        }
        if (groupDtoRequest.getDescription() != null) {
            group.setDescription(groupDtoRequest.getDescription());
        }
        groupRepository.save(group);
        return groupMapper.toDto(group);
    }

    public GroupDtoResponse findByName(String name) {
        Group group = groupRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Группа с названием %s не найдена", name)));
        return groupMapper.toDto(group);
    }


    public void deleteById(long id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format(GROUP_WITH_ID_NOT_FOUND, id)));
        group.getUsers().forEach(user -> user.getGroups().remove(group));
        groupRepository.deleteById(id);
    }
}
