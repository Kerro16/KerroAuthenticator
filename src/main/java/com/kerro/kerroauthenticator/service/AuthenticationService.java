// java
package com.kerro.kerroauthenticator.service;

import com.kerro.kerroauthenticator.dto.LoginUserDTO;
import com.kerro.kerroauthenticator.dto.RegisterUserDTO;
import com.kerro.kerroauthenticator.exception.user.EmailAlreadyExistException;
import com.kerro.kerroauthenticator.exception.user.UserAlreadyExistException;
import com.kerro.kerroauthenticator.exception.user.UserException;
import com.kerro.kerroauthenticator.exception.user.UserNotFoundException;
import com.kerro.kerroauthenticator.model.User;
import com.kerro.kerroauthenticator.repository.UserRepository;
import com.kerro.kerroauthenticator.utils.Constantes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationConfiguration authenticationConfiguration;
    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    public AuthenticationService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 AuthenticationConfiguration authenticationConfiguration) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationConfiguration = authenticationConfiguration;
    }

    public void signup(RegisterUserDTO input) {
        log.info(": {}", input.getUsername());

        if (input.getUsername() == null || input.getUsername().isEmpty()) {
            log.error(Constantes.OBJETOVACIO);
            throw new IllegalArgumentException("Username is empty");
        }

        if (input.getEmail() == null || input.getEmail().isEmpty()) {
            log.error("Email is empty");
            throw new IllegalArgumentException("Email is empty");
        }

        if (userRepository.findByEmail(input.getEmail()).isPresent()) {
            log.warn("Attempt to register with an existing email: {}", input.getEmail());
            throw new EmailAlreadyExistException(input.getEmail());
        }

        if (input.getPassword() == null || input.getPassword().isEmpty()) {
            log.error(Constantes.PASSWORDNULO);
            throw new IllegalArgumentException("Password is empty");
        }

        userRepository.findByUsernameOrEmail(input.getUsername(), input.getEmail()).ifPresent(existingUser -> {
            if (existingUser.getUsername().equals(input.getUsername())) {
                log.warn("Username already exists: {}", input.getUsername());
                throw new UserAlreadyExistException("Username is already in use: " + input.getUsername());
            }
            if (existingUser.getEmail().equals(input.getEmail())) {
                log.warn("Email alredy exists: {}", input.getEmail());
                throw new EmailAlreadyExistException("Email is alredy in use: " + input.getEmail());
            }
        });

        try {
            User user = new User();
            user.setUsername(input.getUsername());
            user.setEmail(input.getEmail());
            user.setRole("user");
            user.setPassword(passwordEncoder.encode(input.getPassword()));

            user = userRepository.save(user);
            log.info("User successfully registered: {}", user.getUsername());
        } catch (Exception ex) {
            log.error("Error registering user: {}", input.getUsername(), ex);
            throw new UserException("Error registering user: " + input.getUsername());
        }
    }

    public User authenticate(LoginUserDTO input) {
        log.info("Login with user: {}", input.getUsername());

        if (input.getUsername() == null || input.getUsername().isEmpty()) {
            log.error("Username is empty");
            throw new UserException(Constantes.OBJETOVACIO);
        }
        if (input.getPassword() == null || input.getPassword().isEmpty()) {
            log.error(Constantes.PASSWORDNULO);
            throw new UserException(Constantes.OBJETOVACIO);
        }

        try {
            boolean isEmail = input.getUsername().contains("@");

            User user = (isEmail
                    ? userRepository.findByEmail(input.getUsername())
                    : userRepository.findByUsername(input.getUsername()))
                    .orElseThrow(() -> {
                        log.warn("User not found for identifier: {}", input.getUsername());
                        return new UserNotFoundException(null);
                    });

            if (!passwordEncoder.matches(input.getPassword(), user.getPassword())) {
                log.warn("Invalid credentials for user: {}", input.getUsername());
                throw new UserException("Invalid credentials");
            }

            log.info("Successfully authenticated: {}", input.getUsername());
            return user;
        } catch (UserNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Authentication error: {}", input.getUsername(), ex);
            throw new UserException("Authentication error");
        }
    }

    public User getUserByUsernameOrEmail(String usernameOrEmail) {
        log.info("Searching user with username or email: {}", usernameOrEmail);

        if (usernameOrEmail == null || usernameOrEmail.isEmpty()) {
            log.error("UsernameOrEmail is empty");
            throw new IllegalArgumentException("UsernameOrEmail is empty");
        }

        try {
            return userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                    .orElseThrow(() -> {
                        log.warn("Username or Email not found: {}", usernameOrEmail);
                        return new UserNotFoundException(null);
                    });
        } catch (Exception ex) {
            log.error("Error with username or email: {}", usernameOrEmail, ex);
            throw new UserException("Error please try again");
        }
    }
}