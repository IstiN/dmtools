package com.github.istin.dmtools.auth.repository;

import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, String> {
    Optional<Workspace> findByNameAndOwner(String name, User owner);
    List<Workspace> findByOwnerOrUsers_User(User owner, User user);
    Optional<Workspace> findByIdAndOwnerOrUsers_User(String id, User owner, User user);
} 