package com.vlad.todo.repository;

import com.vlad.todo.model.Task;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface  TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findById(long id);


    //@Query("SELECT t FROM Task t WHERE t.user.id = ?1")
    @Query(value = "SELECT * FROM tasks WHERE user_id = ?1", nativeQuery = true)
    List<Task> findByUser(long userId);

    void deleteById(long id);
}
