package com.diachuk.todoapp.services.security;

import com.diachuk.todoapp.entities.User;
import com.diachuk.todoapp.entities.security.UserPrincipal;
import com.diachuk.todoapp.entities.security.UserRole;
import com.diachuk.todoapp.services.repositories.RoleRepository;
import com.diachuk.todoapp.services.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findUserByName(username);
        if (!userOptional.isPresent())
            throw new UsernameNotFoundException("User with name " + username + " doesn't exist");
        User user = userOptional.get();
//        user.setRoles(roleRepository.getUserRoles(user.getId()).stream().map(UserRole::new).collect(Collectors.toList()));

        return new UserPrincipal(user);
    }
}
