package com.testit.reports.service;

import com.testit.reports.model.entity.User;
import com.testit.reports.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get all users
     *
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get user by ID
     *
     * @param id User ID
     * @return User
     * @throws EntityNotFoundException if user not found
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    /**
     * Get user by username
     *
     * @param username Username
     * @return Optional of User
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Create a new user
     *
     * @param user User to create
     * @return Created user
     */
    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        return userRepository.save(user);
    }

    /**
     * Update user
     *
     * @param id   User ID
     * @param user User data to update
     * @return Updated user
     * @throws EntityNotFoundException if user not found
     */
    @Transactional
    public User updateUser(Long id, User user) {
        User existingUser = getUserById(id);

        // Check if username is being changed and if it already exists
        if (!existingUser.getUsername().equals(user.getUsername()) && 
                userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }

        // Check if email is being changed and if it already exists
        if (!existingUser.getEmail().equals(user.getEmail()) && 
                userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }

        // Update fields
        existingUser.setUsername(user.getUsername());
        existingUser.setEmail(user.getEmail());
        existingUser.setRole(user.getRole());
        
        // Update password if provided
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        // Update TestIT token if provided
        if (user.getTestitToken() != null) {
            existingUser.setTestitToken(user.getTestitToken());
        }

        return userRepository.save(existingUser);
    }

    /**
     * Update user's TestIT token
     *
     * @param id    User ID
     * @param token TestIT token
     * @return Updated user
     * @throws EntityNotFoundException if user not found
     */
    @Transactional
    public User updateUserToken(Long id, String token) {
        User user = getUserById(id);
        user.setTestitToken(token);
        return userRepository.save(user);
    }

    /**
     * Delete user
     *
     * @param id User ID
     * @throws EntityNotFoundException if user not found
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
