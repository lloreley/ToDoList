package com.vlad.todo.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.vlad.todo.cache.UserCache;
import com.vlad.todo.dto.UserDtoRequest;
import com.vlad.todo.dto.UserDtoResponse;
import com.vlad.todo.exception.AlreadyExistsException;
import com.vlad.todo.exception.NotFoundException;
import com.vlad.todo.mapper.UserMapper;
import com.vlad.todo.model.Group;
import com.vlad.todo.model.User;
import com.vlad.todo.repository.GroupRepository;
import com.vlad.todo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserCache userCache;

    private User existingUser;
    private UserDtoRequest userDtoRequest;
    private UserDtoResponse userDtoResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setFirstName("Vlad");
        existingUser.setLastName("Shcherbo");
        existingUser.setEmail("vlad@gmail.com");
        existingUser.setPhone("+1234567890");
        existingUser.setTasks(new ArrayList<>());
        existingUser.setGroups(new ArrayList<>());
        userDtoRequest = new UserDtoRequest();
        userDtoRequest.setFirstName("Vlad");
        userDtoRequest.setLastName("Shcherbo");
        userDtoRequest.setEmail("vlad@gmail.com");
        userDtoRequest.setPhone("+1234567890");
        userDtoResponse = new UserDtoResponse();
        userDtoResponse.setId(1L);
        userDtoResponse.setFirstName("Vlad");
        userDtoResponse.setLastName("Doe");
        userDtoResponse.setEmail("vlad@gmail.com");
        userDtoResponse.setPhone("+1234567890");
    }

    @Test
    void findAll_ShouldReturnListOfUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(existingUser));
        when(userMapper.toDto(existingUser)).thenReturn(userDtoResponse);

        List<UserDtoResponse> users = userService.findAll();
        assertEquals(1, users.size());
        assertEquals(userDtoResponse, users.get(0));
    }

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(existingUser));
        when(userMapper.toDto(existingUser)).thenReturn(userDtoResponse);

        UserDtoResponse result = userService.findById(1L);
        assertEquals(userDtoResponse, result);
    }

    @Test
    void findById_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.findById(1L));
        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
    }

    @Test
    void findById_ShouldReturnUserFromCache() {
        when(userCache.get(anyLong())).thenReturn(userDtoResponse);

        UserDtoResponse result = userService.findById(1L);
        assertEquals(userDtoResponse, result);
    }

    @Test
    void save_ShouldReturnSavedUser_WhenValidInput() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByPhone(any())).thenReturn(false);
        when(userMapper.toEntity(userDtoRequest)).thenReturn(existingUser);
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toDto(existingUser)).thenReturn(userDtoResponse);

        UserDtoResponse result = userService.save(userDtoRequest);
        assertEquals(userDtoResponse, result);
        verify(userCache).put(existingUser.getId(), userDtoResponse);
    }

    @Test
    void save_ShouldThrowAlreadyExistsException_WhenEmailExists() {
        when(userRepository.existsByEmail(any())).thenReturn(true);

        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class, () -> userService.save(userDtoRequest));
        assertEquals("Пользователь с такой-же почтой/телефоном уже существует", exception.getMessage());
    }

    @Test
    void save_ShouldThrowAlreadyExistsException_WhenPhoneExists() {
        when(userRepository.existsByPhone(any())).thenReturn(true);

        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class, () -> userService.save(userDtoRequest));
        assertEquals("Пользователь с такой-же почтой/телефоном уже существует", exception.getMessage());
    }

    @Test
    void updateUser_ShouldUpdateAndReturnUser_WhenUserExists() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(existingUser));
        when(userMapper.toDto(existingUser)).thenReturn(userDtoResponse);

        var updatedUser = userService.updateUser(1L, userDtoRequest);

        assertEquals(userDtoResponse, updatedUser);
        verify(userRepository).save(existingUser);
        verify(userCache).put(existingUser.getId(), userDtoResponse);

        userDtoRequest.setEmail(null);
        userDtoRequest.setPhone(null);
        userDtoRequest.setLastName(null);
        userDtoRequest.setFirstName(null);

        updatedUser = userService.updateUser(1L, userDtoRequest);
        assertEquals(userDtoResponse, updatedUser);
        verify(userRepository, times(2)).save(existingUser);
        verify(userCache, times(2)).put(existingUser.getId(), userDtoResponse);
    }

    @Test
    void removeUserFromGroup_ShouldThrowNotFoundException_WhenUserNotFound() {
        long userId = 1L;
        long groupId = 1L;
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(new Group()));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.removeUserFromGroup(userId, groupId));
        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
    }

    @Test
    void removeUserFromGroup_ShouldThrowNotFoundException_WhenGroupNotFound() {
        long userId = 1L;
        long groupId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.removeUserFromGroup(userId, groupId));
        assertEquals("Группа с id 1 не найдена", exception.getMessage());
    }

    @Test
    void removeUserFromGroup_ShouldRemoveUser_WhenUserAndGroupExist() {
        long groupId = 1L;
        Group group = mock(Group.class);
        existingUser.setId(1L);
        group.addUser(existingUser);
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        userService.removeUserFromGroup(existingUser.getId(), groupId);
        verify(group).removeUser(existingUser);
    }

    @Test
    void findUsersByGroup_ShouldReturnUserDtos_WhenUsersExist() {
        String groupName = "TestGroup";
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        UserDtoResponse userDtoResponse = new UserDtoResponse();
        userDtoResponse.setId(user.getId());
        userDtoResponse.setEmail(user.getEmail());

        List<User> users = Arrays.asList(user);

        when(userRepository.findUsersByGroupName(groupName)).thenReturn(users);
        when(userMapper.toDto(user)).thenReturn(userDtoResponse);

        List<UserDtoResponse> result = userService.findUsersByGroup(groupName);

        assertEquals(1, result.size());
        assertEquals(userDtoResponse, result.get(0));
    }

    @Test
    void findUsersByGroup_ShouldReturnEmptyList_WhenNoUsersExist() {
        String groupName = "TestGroup";

        when(userRepository.findUsersByGroupName(groupName)).thenReturn(new ArrayList<>());

        List<UserDtoResponse> result = userService.findUsersByGroup(groupName);

        assertTrue(result.isEmpty());
    }

    @Test
    void updateUser_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.updateUser(1L, userDtoRequest));
        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
    }

    @Test
    void deleteUserById_ShouldDeleteUser_WhenUserExists() {
        Group group = new Group();
        group.setUsers(new ArrayList<>(List.of(existingUser)));

        existingUser.setGroups(new ArrayList<>(List.of(group)));

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(existingUser));

        userService.deleteUserById(1L);

        verify(userRepository).deleteById(1L);
        verify(userCache).remove(1L);

        assertFalse(group.getUsers().contains(existingUser), "Пользователь должен быть удалён из группы");
    }

    @Test
    void deleteUserById_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> userService.deleteUserById(1L));
        assertEquals("Пользователь с id 1 не найден", exception.getMessage());
    }


    @Test
    void addUserToGroup_ShouldAddUser_WhenUserAndGroupExist() {
        long userId = existingUser.getId();
        long groupId = 1L;
        Group group = mock(Group.class);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        userService.addUserToGroup(userId, groupId);

        verify(group).addUser(existingUser);
        verify(groupRepository).save(group);
    }

    @Test
    void addUserToGroup_ShouldThrowNotFoundException_WhenGroupDoesNotExist() {
        long userId = existingUser.getId();
        long groupId = 1L;

        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        assertThrows(NotFoundException.class, () -> userService.addUserToGroup(userId, groupId));
    }

    @Test
    void addUserToGroup_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        long userId = existingUser.getId();
        long groupId = 1L;
        Group group = mock(Group.class);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.addUserToGroup(userId, groupId));
    }

    @Test
    void saveAll_ShouldSaveListOfUsers() {
        List<UserDtoRequest> requests = Arrays.asList(userDtoRequest);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByPhone(any())).thenReturn(false);
        when(userMapper.toEntity(userDtoRequest)).thenReturn(existingUser);
        when(userRepository.save(existingUser)).thenReturn(existingUser);
        when(userMapper.toDto(existingUser)).thenReturn(userDtoResponse);

        List<UserDtoResponse> results = userService.saveAll(requests);
        assertEquals(1, results.size());
        assertEquals(userDtoResponse, results.get(0));
    }

    @Test
    void saveAll_ShouldThrowAlreadyExistsException_WhenUserExistsByEmail() {
        List<UserDtoRequest> requests = Arrays.asList(userDtoRequest);
        when(userRepository.existsByEmail(any())).thenReturn(true);

        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class, () -> userService.saveAll(requests));
        assertEquals("Пользователь с такой-же почтой/телефоном уже существует", exception.getMessage());
    }

    @Test
    void saveAll_ShouldThrowAlreadyExistsException_WhenUserExistsByPhone() {
        List<UserDtoRequest> requests = Arrays.asList(userDtoRequest);
        when(userRepository.existsByPhone(any())).thenReturn(true);

        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class, () -> userService.saveAll(requests));
        assertEquals("Пользователь с такой-же почтой/телефоном уже существует", exception.getMessage());
    }
}