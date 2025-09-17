package com.vlad.todo.repository;

import com.vlad.todo.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findById(long id);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    void deleteById(long id);

    @Query("SELECT u FROM User u JOIN u.groups g WHERE g.name = :groupName")
    List<User> findUsersByGroupName(@Param("groupName") String groupName);



}
