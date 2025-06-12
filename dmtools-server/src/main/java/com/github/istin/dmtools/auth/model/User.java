package com.github.istin.dmtools.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    private String id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private boolean emailVerified;
    private String name;
    private String givenName;
    private String familyName;
    private String pictureUrl;
    private String locale;
    
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;
    
    private String providerId;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles;
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_credentials", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyColumn(name = "credential_key")
    @Column(name = "credential_value")
    private Map<String, List<String>> credentials;

} 