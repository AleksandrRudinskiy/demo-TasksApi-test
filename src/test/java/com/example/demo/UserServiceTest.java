package com.example.demo;


import com.example.demo.exception.NotUniqueUsernameException;
import com.example.demo.user.Role;
import com.example.demo.user.User;
import com.example.demo.user.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceTest {

    private final EntityManager em;
    private final UserService service;

    @Test
    void saveUserTest() {
        User newuser = makeUser(1L, "username", "password", "email@mail.ru", Role.ROLE_USER);
        service.save(newuser);
        Query query = em.createNativeQuery("Select * from users where email = :email", User.class);
        User user = (User) query.setParameter("email", newuser.getEmail()).getSingleResult();
        assertThat(user.getUsername(), equalTo(newuser.getUsername()));
        assertThat(user.getPassword(), equalTo(newuser.getPassword()));
        assertThat(user.getEmail(), equalTo(newuser.getEmail()));
        assertThat(user.getRole(), equalTo(newuser.getRole()));
    }

    @Test
    void getUserByUserNameTest() {
        User newuser = makeUser(1L, "username", "password", "email@mail.ru", Role.ROLE_USER);
        service.save(newuser);

        User user = service.getByUsername("username");
        assertThat(user.getUsername(), equalTo(newuser.getUsername()));
        assertThat(user.getPassword(), equalTo(newuser.getPassword()));
        assertThat(user.getEmail(), equalTo(newuser.getEmail()));
        assertThat(user.getRole(), equalTo(newuser.getRole()));
    }

    @Test
    void createUserTest() {
        User newuser = makeUser(1L, "username", "password", "email@mail.ru", Role.ROLE_USER);
        service.create(newuser);
        Query query = em.createNativeQuery("Select * from users where email = :email", User.class);
        User user = (User) query.setParameter("email", newuser.getEmail()).getSingleResult();
        assertThat(user.getUsername(), equalTo(newuser.getUsername()));
        assertThat(user.getPassword(), equalTo(newuser.getPassword()));
        assertThat(user.getEmail(), equalTo(newuser.getEmail()));
        assertThat(user.getRole(), equalTo(newuser.getRole()));
    }

    @Test
    void whenDerivedExceptionThrown_thenAssertionSucceeds() {
        User newuser1 = makeUser(1L, "username", "password", "email@mail.ru", Role.ROLE_USER);
        service.save(newuser1);
        User newuser2 = makeUser(2L, "username", "password", "rewfemail@mail.ru", Role.ROLE_USER);
        Exception exception = assertThrows(NotUniqueUsernameException.class, () -> {
            service.create(newuser2);
        });

        String expectedMessage = "Пользователь с именем " + newuser2.getUsername() + " уже существует";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void getCurrentUserTest() {
        User newuser1 = makeUser(1L, "username", "password", "email@mail.ru", Role.ROLE_USER);
        service.save(newuser1);
        Authentication authentication = Mockito.mock(Authentication.class);
// Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        Mockito
                .when(authentication.getName())
                .thenReturn("username");

        User user = service.getCurrentUser("username");
        assertThat(user.getUsername(), equalTo("username"));

    }

    private User makeUser(
            Long id, String username, String password, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }
}
