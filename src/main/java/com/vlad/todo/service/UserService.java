package com.vlad.todo.service;

import static com.vlad.todo.service.GroupService.GROUP_WITH_ID_NOT_FOUND;

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
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Transactional
public class UserService {

    public static final String USER_WITH_ID_NOT_FOUND = "Пользователь с id %d не найден";
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final UserCache userCache;

    public List<UserDtoResponse> findAll() {
        List<UserDtoResponse> usersDtoResponse = new ArrayList<>();
        userRepository.findAll().forEach(
                user -> usersDtoResponse.add(userMapper.toDto(user)));
        return usersDtoResponse;
    }

    public UserDtoResponse findById(long id) {
        UserDtoResponse cachedUser = userCache.get(id);
        if (cachedUser != null) {
            return cachedUser;
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format(USER_WITH_ID_NOT_FOUND, id)));
        userCache.put(id, userMapper.toDto(user));
        return userMapper.toDto(user);
    }

    public UserDtoResponse save(UserDtoRequest userDtoRequest) {
        if (userRepository.existsByEmail(userDtoRequest.getEmail())
                || userRepository.existsByPhone(userDtoRequest.getPhone())) {
            throw new AlreadyExistsException(
                    "Пользователь с такой-же почтой/телефоном уже существует");
        }
        User user = userMapper.toEntity(userDtoRequest);
        userRepository.save(user);
        userCache.put(user.getId(), userMapper.toDto(user));
        return userMapper.toDto(user);
    }

    public UserDtoResponse updateUser(long id, UserDtoRequest userDtoRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format(USER_WITH_ID_NOT_FOUND, id)));

        if (userDtoRequest.getEmail() != null) {
            user.setEmail(userDtoRequest.getEmail());
        }
        if (userDtoRequest.getPhone() != null) {
            user.setPhone(userDtoRequest.getPhone());
        }
        if (userDtoRequest.getLastName() != null) {
            user.setLastName(userDtoRequest.getLastName());
        }
        if (userDtoRequest.getFirstName() != null) {
            user.setFirstName(userDtoRequest.getFirstName());
        }
        userRepository.save(user);
        userCache.put(user.getId(), userMapper.toDto(user));
        return userMapper.toDto(user);
    }

    public void deleteUserById(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format(USER_WITH_ID_NOT_FOUND, id)));
        user.getGroups().forEach(group -> group.getUsers().remove(user));
        userCache.remove(id);
        userRepository.deleteById(id);
    }

    public List<UserDtoResponse> findUsersByGroup(String groupName) {
        List<User> users = userRepository.findUsersByGroupName(groupName);
        List<UserDtoResponse> usersDtoResponse = new ArrayList<>();
        users.forEach(user -> usersDtoResponse.add(userMapper.toDto(user)));
        return usersDtoResponse;
    }

    public void addUserToGroup(long userId, long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(GROUP_WITH_ID_NOT_FOUND, groupId)));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(USER_WITH_ID_NOT_FOUND, userId)));
        group.addUser(user);
        groupRepository.save(group);
    }

    public void removeUserFromGroup(long userId, long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(GROUP_WITH_ID_NOT_FOUND, groupId)));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(USER_WITH_ID_NOT_FOUND, userId)));
        group.removeUser(user);
        groupRepository.save(group);
    }

    @Transactional
    public List<UserDtoResponse> saveAll(List<UserDtoRequest> userDtoRequests) {
        return userDtoRequests.stream()
                .map(userDtoRequest -> {
                    if (userRepository.existsByEmail(userDtoRequest.getEmail())
                            || userRepository.existsByPhone(userDtoRequest.getPhone())) {
                        throw new AlreadyExistsException(
                                "Пользователь с такой-же почтой/телефоном уже существует");
                    }

                    User user = userMapper.toEntity(userDtoRequest);
                    userRepository.save(user);
                    return userMapper.toDto(user);
                })
                .toList();
    }
}
