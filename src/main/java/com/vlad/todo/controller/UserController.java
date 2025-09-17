package com.vlad.todo.controller;

import com.vlad.todo.dto.UserDtoRequest;
import com.vlad.todo.dto.UserDtoResponse;
import com.vlad.todo.exception.InvalidInputException;
import com.vlad.todo.exception.NotFoundException;
import com.vlad.todo.service.GroupService;
import com.vlad.todo.service.UserService;
import com.vlad.todo.service.VisitCounterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Пользователи", description = "API для управления пользователями")
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final GroupService groupService;
    private final VisitCounterService visitCounterService;


    @Autowired
    public UserController(UserService userService, GroupService groupService,
                          VisitCounterService visitCounterService) {
        this.userService = userService;
        this.groupService = groupService;
        this.visitCounterService = visitCounterService;
    }

    @Operation(summary = "Получить всех пользователей",
            description = "Возвращает список всех пользователей")
    @GetMapping
    public ResponseEntity<List<UserDtoResponse>> allUsers() {
        visitCounterService.increment();
        return ResponseEntity.ok(userService.findAll());
    }


    @Operation(summary = "Создать пользователя",
            description = "Создает нового пользователя и возвращает его данные")
    @PostMapping("/saveUser")
    public ResponseEntity<UserDtoResponse> saveUser(
            @Parameter(description = "Данные нового пользователя")
            @Valid @RequestBody UserDtoRequest userDtoRequest) {
        UserDtoResponse savedUser = userService.save(userDtoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @Operation(summary = "Получить пользователей по группе",
            description = "Возвращает список пользователей в указанной группе")
    @GetMapping("/by-group/{groupName}")
    public ResponseEntity<List<UserDtoResponse>> findUsersByGroup(
            @Parameter(description = "Название группы")
            @PathVariable String groupName) {
        if (groupService.findByName(groupName) == null) {
            throw new NotFoundException("Группа с названием " + groupName + " не найдена");
        }
        return ResponseEntity.ok(userService.findUsersByGroup(groupName));
    }

    @Operation(summary = "Получить пользователя по ID",
            description = "Возвращает пользователя по его уникальному идентификатору")
    @GetMapping("/{id}")
    public ResponseEntity<UserDtoResponse> findUserById(
            @Parameter(description = "ID пользователя")
            @PathVariable long id) {
        if (id < 1) {
            throw new InvalidInputException("Id должно быть больше 0");
        }
        return ResponseEntity.ok(userService.findById(id));
    }


    @Operation(summary = "Обновить пользователя",
            description = "Обновляет информацию о пользователе по его ID")
    @PutMapping("/{id}")
    public ResponseEntity<UserDtoResponse> updateUser(
            @Parameter(description = "ID пользователя")
            @PathVariable long id,
            @Parameter(description = "Обновленные данные пользователя")
            @RequestBody UserDtoRequest userDtoRequest) {
        if (id < 1) {
            throw new InvalidInputException("Id должно быть больше 0");
        }
        return ResponseEntity.ok(userService.updateUser(id, userDtoRequest));
    }

    @Operation(summary = "Удалить пользователя",
            description = "Удаляет пользователя по его ID")
    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя")
            @PathVariable long id) {
        if (id < 1) {
            throw new InvalidInputException("Id должно быть больше 0");
        }
        userService.deleteUserById(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Добавить пользователя в группу",
            description = "Добавляет пользователя в указанную группу")
    @PostMapping("/{userId}/add/{groupId}")
    public ResponseEntity<Void> addUserToGroup(
            @Parameter(description = "ID пользователя")
            @PathVariable long userId,
            @Parameter(description = "ID группы")
            @PathVariable long groupId) {
        if (userId < 1 || groupId < 1) {
            throw new InvalidInputException("Ids должны быть больше 0");
        }
        userService.addUserToGroup(userId, groupId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить пользователя из группы",
            description = "Удаляет пользователя из указанной группы")
    @DeleteMapping("/{userId}/remove/{groupId}")
    public ResponseEntity<Void> removeUserFromGroup(
            @Parameter(description = "ID пользователя")
            @PathVariable long userId,
            @Parameter(description = "ID группы")
            @PathVariable long groupId) {
        if (userId < 1 || groupId < 1) {
            throw new InvalidInputException("Ids должны быть больше 0");
        }
        userService.removeUserFromGroup(userId, groupId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Создать нескольких пользователей",
            description = "Создает новых пользователей и возвращает их данные")
    @PostMapping("/saveAll")
    public ResponseEntity<List<UserDtoResponse>> saveAllUsers(
            @Parameter(description = "Данные новых пользователей")
            @Valid @RequestBody List<UserDtoRequest> userDtoRequests) {
        log.info("here");
        List<UserDtoResponse> savedUsers = userService.saveAll(userDtoRequests);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUsers);
    }


}