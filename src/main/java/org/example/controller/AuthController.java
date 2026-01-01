package org.example.controller;

import org.example.model.User;
import org.example.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private record UserRequestDTO(
            @NotBlank String name,
            @NotBlank String username,
            @NotBlank String password
    ) {}

    private record UserResponseDTO(
            Long id,
            String name,
            String username,
            String role
    ) {}

    @PostMapping("/user")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserRequestDTO request) {
        if (userRepository.findByUsernameIgnoreCase(request.username()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }

        User user = new User();
        user.setName(request.name());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));

        if (userRepository.count() == 0) {
            user.setRole("ADMINISTRATOR");
            user.setAccountNonLocked(true);
        } else {
            user.setRole("MERCHANT");
            user.setAccountNonLocked(false);
        }

        User savedUser = userRepository.save(user);

        return new ResponseEntity<>(
                new UserResponseDTO(savedUser.getId(), savedUser.getName(), savedUser.getUsername(), savedUser.getRole()),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/list")
    public List<UserResponseDTO> listUsers() {
        return userRepository.findAll(Sort.by("id").ascending()).stream()
                .map(u -> new UserResponseDTO(u.getId(), u.getName(), u.getUsername(), u.getRole()))
                .collect(Collectors.toList());
    }

    private record DeletedUserDTO(
            String username,
            String status
    ) {}

    @DeleteMapping("/user/{username}")
    public DeletedUserDTO deleteUser(@PathVariable String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userRepository.delete(user);

        return new DeletedUserDTO(username, "Deleted successfully!");
    }

    public record ChangeRoleRequestDTO(
            @NotBlank String username,
            @NotBlank @Pattern(regexp = "SUPPORT|MERCHANT", message = "Role must be SUPPORT or MERCHANT") String role
    ) {}

    @PutMapping("/role")
    public UserResponseDTO changeRole(@Valid @RequestBody ChangeRoleRequestDTO request) {
        User user = userRepository.findByUsernameIgnoreCase(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole().equals(request.role())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Role is already assigned");
        }

        user.setRole(request.role());
        userRepository.save(user);

        return new UserResponseDTO(user.getId(), user.getName(), user.getUsername(), user.getRole());
    }

    public record ChangeAccessRequestDTO(
            @NotBlank String username,
            @NotBlank @Pattern(regexp = "LOCK|UNLOCK", message = "Operation must be LOCK or UNLOCK") String operation
    ) {}

    @PutMapping("/access")
    public Map<String, String> changeAccess(@Valid @RequestBody ChangeAccessRequestDTO request) {
        User user = userRepository.findByUsernameIgnoreCase(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole().equals("ADMINISTRATOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot block Administrator");
        }

        boolean lockOperation = request.operation().equals("LOCK");
        user.setAccountNonLocked(!lockOperation);

        userRepository.save(user);

        String statusMsg = "User " + user.getUsername() + " " + (lockOperation ? "locked" : "unlocked") + "!";
        return Map.of("status", statusMsg);
    }
}
