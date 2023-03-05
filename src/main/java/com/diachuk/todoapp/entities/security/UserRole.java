package com.diachuk.todoapp.entities.security;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NamedNativeQuery(name = "UserRole.getUserRoles", query = "SELECT r.role FROM roles as r JOIN _users_roles ur on r.id = ur.roles_id WHERE ur.user_id = ?1")
@NoArgsConstructor
public class UserRole implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true)
    private String role;


    public UserRole(String role){
        this.role = role;
    }
    @Override
    public String getAuthority() {
        return role;
    }
}
