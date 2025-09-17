package com.vlad.todo.repository;

import com.vlad.todo.model.Group;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findById(long id);

    Optional<Group> findByName(String name);

    void deleteById(long id);
}
