package com.example.demo;

import com.example.demo.user.Role;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTest {
    private final UserRepository userRepository;

    @Test
    void testAddUser() {
        User user = new User(
                1L,
                "username1",
                "password",
                "email@mail.ru",
                Role.ROLE_USER);


        userRepository.save(user);
        Assertions.assertEquals(
                1,
                userRepository.findAll().size(),
                "Количество пользователей должно быть 1!");
        assertThat(user)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(userRepository.findById(1L).get());
    }
}

