package com.example.demo.user;

import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;


public interface UserService {

    User save(User user);

    User create(User user);

    User getByUsername(String username);

    UserDetailsService userDetailsService();

    User getCurrentUser();

    List<User> getAll(int from, int size);

    void deleteUser(Long id);

}
