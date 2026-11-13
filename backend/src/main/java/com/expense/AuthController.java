package com.expense;

import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public record RegisterRequest(@NotBlank @Size(max=80) String name, @NotBlank @Email @Size(max=254) String email, @NotBlank @Size(min=8,max=72) String password) {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    private final UserRepository users; private final PasswordEncoder passwords; private final JwtService jwt;
    public AuthController(UserRepository users, PasswordEncoder passwords, JwtService jwt) { this.users=users; this.passwords=passwords; this.jwt=jwt; }
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> register(@Valid @RequestBody RegisterRequest body) {
        String email = body.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Email is already registered");
        User user = new User(); user.setName(body.name().trim()); user.setEmail(email); user.setPassword(passwords.encode(body.password()));
        try { user=users.save(user); } catch (DuplicateKeyException ex) { throw new ResponseStatusException(HttpStatus.CONFLICT,"Email is already registered"); }
        return Map.of("token",jwt.create(user.getId()),"user",Map.of("id",user.getId(),"name",user.getName(),"email",user.getEmail()));
    }
    @PostMapping("/login")
    public Map<String,Object> login(@Valid @RequestBody LoginRequest body) {
        User user=users.findByEmail(body.email().trim().toLowerCase()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password"));
        if (!passwords.matches(body.password(),user.getPassword())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid email or password");
        return Map.of("token",jwt.create(user.getId()),"user",Map.of("id",user.getId(),"name",user.getName(),"email",user.getEmail()));
    }
}
