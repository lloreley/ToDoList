package com.vlad.todo.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.vlad.todo.dto.GroupDtoRequest;
import com.vlad.todo.dto.GroupDtoResponse;
import com.vlad.todo.exception.NotFoundException;
import com.vlad.todo.mapper.GroupMapper;
import com.vlad.todo.model.Group;
import com.vlad.todo.model.User;
import com.vlad.todo.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class GroupServiceTest {

    @InjectMocks
    private GroupService groupService;

    @Mock
    private GroupMapper groupMapper;

    @Mock
    private GroupRepository groupRepository;

    private Group existingGroup;
    private GroupDtoRequest groupDtoRequest;
    private GroupDtoResponse groupDtoResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        existingGroup = new Group();
        existingGroup.setId(1L);
        existingGroup.setName("Test Group");
        existingGroup.setDescription("Description of Test Group");

        groupDtoRequest = new GroupDtoRequest();
        groupDtoRequest.setName("Updated Group");
        groupDtoRequest.setDescription("Updated Description");

        groupDtoResponse = new GroupDtoResponse();
        groupDtoResponse.setId(1L);
        groupDtoResponse.setName("Test Group");
        groupDtoResponse.setDescription("Description of Test Group");
    }

    @Test
    void findAll_ShouldReturnListOfGroups() {
        when(groupRepository.findAll()).thenReturn(Arrays.asList(existingGroup));
        when(groupMapper.toDto(existingGroup)).thenReturn(groupDtoResponse);

        List<GroupDtoResponse> groups = groupService.findAll();
        assertEquals(1, groups.size());
        assertEquals(groupDtoResponse, groups.get(0));
    }

    @Test
    void findById_ShouldReturnGroup_WhenGroupExists() {
        when(groupRepository.findById(anyLong())).thenReturn(Optional.of(existingGroup));
        when(groupMapper.toDto(existingGroup)).thenReturn(groupDtoResponse);

        GroupDtoResponse result = groupService.findById(1L);
        assertEquals(groupDtoResponse, result);
    }

    @Test
    void findById_ShouldThrowNotFoundException_WhenGroupDoesNotExist() {
        when(groupRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> groupService.findById(1L));
        assertEquals("Группа с id 1 не найдена", exception.getMessage());
    }

    @Test
    void save_ShouldReturnSavedGroup_WhenValidInput() {
        when(groupMapper.toEntity(groupDtoRequest)).thenReturn(existingGroup);
        when(groupRepository.save(existingGroup)).thenReturn(existingGroup);
        when(groupMapper.toDto(existingGroup)).thenReturn(groupDtoResponse);

        GroupDtoResponse result = groupService.save(groupDtoRequest);
        assertEquals(groupDtoResponse, result);
    }

    @Test
    void update_ShouldUpdateAndReturnGroup_WhenGroupExists() {
        when(groupRepository.findById(anyLong())).thenReturn(Optional.of(existingGroup));
        when(groupMapper.toDto(existingGroup)).thenReturn(groupDtoResponse);

        var updatedGroup = groupService.update(1L, groupDtoRequest);

        assertEquals(groupDtoResponse, updatedGroup);
        verify(groupRepository).save(existingGroup);
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenGroupDoesNotExist() {
        when(groupRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> groupService.update(1L, groupDtoRequest));
        assertEquals("Группа с id 1 не найдена", exception.getMessage());
    }

    @Test
    void update_ShouldUpdateNameAndDescription_WhenProvided() {
        when(groupRepository.findById(anyLong())).thenReturn(Optional.of(existingGroup));
        when(groupMapper.toDto(existingGroup)).thenReturn(groupDtoResponse);

        groupService.update(1L, groupDtoRequest);

        assertEquals("Updated Group", existingGroup.getName());
        assertEquals("Updated Description", existingGroup.getDescription());
    }

    @Test
    void update_ShouldNotChangeName_WhenNotProvided() {
        groupDtoRequest.setName(null);
        groupDtoRequest.setDescription("New Description");

        when(groupRepository.findById(anyLong())).thenReturn(Optional.of(existingGroup));

        groupService.update(1L, groupDtoRequest);

        assertEquals("New Description", existingGroup.getDescription());
        assertEquals("Test Group", existingGroup.getName());
    }

    @Test
    void update_ShouldNotChangeDescription_WhenNotProvided() {
        groupDtoRequest.setDescription(null);
        groupDtoRequest.setName("New Name");

        when(groupRepository.findById(anyLong())).thenReturn(Optional.of(existingGroup));

        groupService.update(1L, groupDtoRequest);

        assertEquals("New Name", existingGroup.getName());
        assertEquals("Description of Test Group", existingGroup.getDescription());
    }

    @Test
    void findByName_ShouldReturnGroup_WhenGroupExists() {
        when(groupRepository.findByName(any())).thenReturn(Optional.of(existingGroup));
        when(groupMapper.toDto(existingGroup)).thenReturn(groupDtoResponse);

        GroupDtoResponse result = groupService.findByName("Test Group");
        assertEquals(groupDtoResponse, result);
    }

    @Test
    void findByName_ShouldThrowNotFoundException_WhenGroupDoesNotExist() {
        when(groupRepository.findByName(any())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> groupService.findByName("Nonexistent Group"));
        assertEquals("Группа с названием Nonexistent Group не найдена", exception.getMessage());
    }

    @Test
    void deleteById_ShouldDeleteGroup_WhenGroupExists() {
        when(groupRepository.findById(anyLong())).thenReturn(Optional.of(existingGroup));

        groupService.deleteById(1L);

        verify(groupRepository).deleteById(1L);
    }

    @Test
    void deleteById_ShouldThrowNotFoundException_WhenGroupDoesNotExist() {
        when(groupRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> groupService.deleteById(1L));
        assertEquals("Группа с id 1 не найдена", exception.getMessage());
    }

    @Test
    void deleteById_ShouldRemoveGroupFromUsers_WhenGroupIsDeleted() {
        User user = new User();
        user.setId(1L);
        user.getGroups().add(existingGroup);
        existingGroup.getUsers().add(user);

        when(groupRepository.findById(anyLong())).thenReturn(Optional.of(existingGroup));

        groupService.deleteById(1L);

        verify(groupRepository).deleteById(1L);
        assertFalse(user.getGroups().contains(existingGroup), "Группа должна быть удалена из пользователя");
    }
}