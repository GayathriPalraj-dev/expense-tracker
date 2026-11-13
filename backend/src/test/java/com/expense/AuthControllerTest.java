package com.expense;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Mock private UserRepository users;
    @Mock private JwtService jwt;
    private final PasswordEncoder passwords = new BCryptPasswordEncoder();
    private AuthController controller;

    @BeforeEach
    void setUp() { controller = new AuthController(users, passwords, jwt); }

    @Test
    void registrationStoresBcryptHashInMongoRepository() throws Exception {
        when(users.existsByEmail("user@example.com")).thenReturn(false);
        when(users.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            Field id = User.class.getDeclaredField("id");
            id.setAccessible(true);
            id.set(saved, "user-1");
            return saved;
        });
        when(jwt.create("user-1")).thenReturn("signed-token");

        controller.register(new AuthController.RegisterRequest("A User", "USER@example.com", "correct-horse"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertEquals("user@example.com", saved.getValue().getEmail());
        assertTrue(passwords.matches("correct-horse", saved.getValue().getPassword()));
        assertTrue(saved.getValue().getPassword().startsWith("$2a$") || saved.getValue().getPassword().startsWith("$2b$"));
    }

    @Test
    void loginLoadsMongoUserAndChecksBcryptPassword() {
        User user = new User();
        setId(user, "user-1");
        user.setEmail("user@example.com");
        user.setName("A User");
        user.setPassword(passwords.encode("correct-horse"));
        when(users.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwt.create("user-1")).thenReturn("signed-token");

        controller.login(new AuthController.LoginRequest("USER@example.com", "correct-horse"));

        verify(users).findByEmail("user@example.com");
        verify(jwt).create(user.getId());
    }

    private void setId(User user, String value) {
        try {
            Field id = User.class.getDeclaredField("id");
            id.setAccessible(true);
            id.set(user, value);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
}
