// java
package com.kerro.kerroauthenticator.controller;

import com.kerro.kerroauthenticator.dto.LoginUserDTO;
import com.kerro.kerroauthenticator.dto.RegisterUserDTO;
import com.kerro.kerroauthenticator.exception.user.EmailAlreadyExistException;
import com.kerro.kerroauthenticator.exception.user.UserAlreadyExistException;
import com.kerro.kerroauthenticator.exception.user.UserNotFoundException;
import com.kerro.kerroauthenticator.jwt.JwtService;
import com.kerro.kerroauthenticator.jwt.TokenBlacklistService;
import com.kerro.kerroauthenticator.model.User;
import com.kerro.kerroauthenticator.service.AuthenticationService;
import com.kerro.kerroauthenticator.utils.Constantes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {


    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final TokenBlacklistService tokenBlacklistService;
    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    public AuthenticationController(JwtService jwtService,
                                    AuthenticationService authenticationService,
                                    TokenBlacklistService tokenBlacklistService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> register(@Validated @RequestBody RegisterUserDTO registerUserDto) {
        try {
            authenticationService.signup(registerUserDto);
            return ResponseEntity.ok(Map.of(Constantes.MESSAGE, "Register successfully"));
        } catch (IllegalArgumentException ex) {
            log.error("Registration error: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of(Constantes.ERROR, ex.getMessage()));
        } catch (UserAlreadyExistException | EmailAlreadyExistException ex) {
            log.warn("Registration error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(Constantes.ERROR, ex.getMessage()));
        } catch (Exception ex) {
            log.error(Constantes.UNEXPECTED, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of( Constantes.ERROR, Constantes.UNEXPECTED));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> authenticate(@RequestBody LoginUserDTO loginUserDto) {
        try {
            User authenticatedUser = authenticationService.authenticate(loginUserDto);
            String jwtToken = jwtService.generateToken(authenticatedUser); // ahora existe overload sin TTL

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "token", "Bearer " + jwtToken
            ));
        } catch (IllegalArgumentException ex) {
            log.error("Authentication error: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of(Constantes.ERROR, ex.getMessage()));
        } catch (UserNotFoundException ex) {
            log.warn("User not found: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(Constantes.ERROR, ex.getMessage()));
        } catch (Exception ex) {
            log.error(Constantes.UNEXPECTED, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(Constantes.ERROR, Constantes.UNEXPECTED));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        log.info("Logout request received");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("message", "No Authorization Bearer token provided"));
        }

        String token = authorizationHeader.substring(7).trim();

        if (tokenBlacklistService.isBlacklisted(token)) {
            log.warn("Token already blacklisted");
            return ResponseEntity.ok(Map.of("message", "Token already invalidated"));
        }

        try {
            long expiryEpochMillis = jwtService.getExpirationEpochMillis(token);
            tokenBlacklistService.blacklistToken(token, expiryEpochMillis);
            log.info("Token blacklisted until {}", expiryEpochMillis);
            return ResponseEntity.ok(Map.of("message", "Logout successful"));
        } catch (Exception ex) {
            log.error("Error during logout token invalidation", ex);
            long defaultTtlMillis = System.currentTimeMillis() + 1000L * 60 * 60; // 1 hora por defecto
            tokenBlacklistService.blacklistToken(token, defaultTtlMillis);
            return ResponseEntity.ok(Map.of("message", "Logout processed (fallback TTL)"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getUserRole(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Me request Received: {}", userDetails.getUsername());
        if (userDetails.getUsername() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            log.info("Obtaining unsername: {}", userDetails.getUsername());
            User user = authenticationService.getUserByUsernameOrEmail(userDetails.getUsername());
            Map<String, String> role = Map.of("role", user.getRole());
            log.info("User role: {} es: {}", user.getUsername(), user.getRole());
            log.info("Me: Successful");
            return ResponseEntity.ok(role);
        } catch (Exception ex){
            throw new UserNotFoundException(null);
        }
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Info request received for user: {}", userDetails.getUsername());

        if (userDetails.getUsername() == null) {
            log.error("UserDetails is null or username is missing");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = authenticationService.getUserByUsernameOrEmail(userDetails.getUsername());

        Map<String, Object> userInfo = Map.of(
                "id", user.getId(),
                "username", user.getUsername()
        );

        log.info("User info retrieved successfully for username: {}", user.getUsername());
        return ResponseEntity.ok(userInfo);
    }
}