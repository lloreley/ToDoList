package com.vlad.todo.controller;

import com.vlad.todo.dto.GroupDtoRequest;
import com.vlad.todo.dto.GroupDtoResponse;
import com.vlad.todo.dto.UserDtoResponse;
import com.vlad.todo.exception.InvalidInputException;
import com.vlad.todo.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Группы", description = "API для управления группами пользователей")
@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/groups")
public class GroupController {
    private final GroupService groupService;

    @Autowired
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping("/{id}/users")
    public ResponseEntity<List<UserDtoResponse>> getUsersByGroupId(@PathVariable Long id) {
        GroupDtoResponse group = groupService.findById(id);
        return ResponseEntity.ok(group.getUsers().stream().toList());
    }

    @Operation(summary = "Получить все группы",
            description = "Возвращает список всех доступных групп")
    @GetMapping
    public ResponseEntity<List<GroupDtoResponse>> getAllGroups() {
        return ResponseEntity.ok(groupService.findAll());
    }

    @Operation(summary = "Создать новую группу",
            description = "Создает новую группу и возвращает её данные")
    @PostMapping("/saveGroup")
    public ResponseEntity<GroupDtoResponse> saveGroup(
            @Parameter(description = "Данные новой группы")
            @Valid @RequestBody GroupDtoRequest groupDtoRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.save(groupDtoRequest));
    }

    @Operation(summary = "Обновить группу",
            description = "Обновляет информацию об группе по её ID и возвращает обновлённые данные")
    @PutMapping("/{id}")
    public ResponseEntity<GroupDtoResponse> updateGroup(
            @Parameter(description = "ID группы")
            @PathVariable long id,
            @Parameter(description = "Обновленные данные группы")
            @RequestBody GroupDtoRequest groupDtoRequest) {
        if (id < 1) {
            throw new InvalidInputException("Id должно быть больше 0");
        }
        return ResponseEntity.ok(groupService.update(id, groupDtoRequest));
    }

    @Operation(summary = "Получить группу по ID", description = "Возвращает группу по её ID")
    @GetMapping("/{id}")
    public ResponseEntity<GroupDtoResponse> findGroupById(
            @Parameter(description = "ID группы")
            @PathVariable long id) {
        if (id < 1) {
            throw new InvalidInputException("Id должно быть больше 0");
        }
        return ResponseEntity.ok(groupService.findById(id));
    }

    @Operation(summary = "Удалить группу", description = "Удаляет группу по её ID")
    @DeleteMapping("/deleteGroup/{id}")
    public ResponseEntity<Void> deleteGroupById(
            @Parameter(description = "ID группы")
            @PathVariable long id) {
        if (id < 1) {
            throw new InvalidInputException("Id должно быть больше 0");
        }
        groupService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}