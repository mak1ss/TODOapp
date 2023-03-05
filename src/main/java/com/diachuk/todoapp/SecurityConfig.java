package com.diachuk.todoapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("/", "/user/registration", "/user/perform_registration").access("permitAll()")
                .mvcMatchers("/user/**", "/tasks/**").hasRole("USER")
                .mvcMatchers("/user/admin/**").access("isFullyAuthenticated() and hasIpAddress('188.163.38.140') and hasRole('ADMIN')")
                .and()
                .formLogin().loginPage("/user/login").permitAll().failureUrl("/user/login?error=true").loginProcessingUrl("/login_perform").defaultSuccessUrl("/user/")
                .and()
                .logout().logoutUrl("/user/logout").permitAll().logoutSuccessUrl("/user/login?logout=true")
                .and()
                .exceptionHandling().accessDeniedPage("/user/access-denied")
                .and()
                .rememberMe()
                .tokenValiditySeconds(120)
                .key("secretTokenKey")
                .and()
                .httpBasic();
        return http.build();
    }
}
