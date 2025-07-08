package com.github.istin.dmtools.auth.repository;

import com.github.istin.dmtools.auth.model.Integration;
import com.github.istin.dmtools.auth.model.User;
import com.github.istin.dmtools.auth.model.Workspace;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationRepository extends JpaRepository<Integration, String> {
    
    /**
     * Find integrations by creator
     */
    List<Integration> findByCreatedBy(User user);
    
    /**
     * Find integrations by type
     */
    List<Integration> findByType(String type);
    
    /**
     * Find integrations by name containing a string (case insensitive)
     */
    List<Integration> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find integrations accessible to a user (either as creator or shared with)
     */
    @Query("SELECT DISTINCT i FROM Integration i LEFT JOIN i.users iu WHERE i.createdBy = :user OR iu.user = :user")
    List<Integration> findAccessibleToUser(@Param("user") User user);
    
    /**
     * Find integrations accessible in a workspace
     */
    @Query("SELECT DISTINCT i FROM Integration i JOIN i.workspaces iw WHERE iw.workspace = :workspace")
    List<Integration> findByWorkspace(@Param("workspace") Workspace workspace);
    
    /**
     * Find integration by ID and creator
     */
    Optional<Integration> findByIdAndCreatedBy(String id, User user);
    
    /**
     * Find enabled integrations by type
     */
    List<Integration> findByTypeAndEnabledTrue(String type);
    
    /**
     * Find integration by ID with config params eagerly loaded
     */
    @EntityGraph(attributePaths = {"configParams"})
    @Query("SELECT i FROM Integration i WHERE i.id = :id")
    Optional<Integration> findByIdWithConfigParams(@Param("id") String id);
} 