package com.projectjend.demo.controller;

import com.projectjend.demo.dto.*;
import com.projectjend.demo.entity.Account;
import com.projectjend.demo.security.JwtUtil;
import com.projectjend.demo.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AccountService accountService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AccountService accountService, 
                         AuthenticationManager authenticationManager, 
                         JwtUtil jwtUtil) {
        this.accountService = accountService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AccountDTO accountDTO) {
        try {
            Account account = accountService.createAccount(accountDTO);
            String token = jwtUtil.generateToken(account.getEmail());
            
            AuthResponseDTO response = new AuthResponseDTO(
                token,
                account.getEmail(),
                account.getName(),
                account.getId()
            );
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDTO.email(),
                    loginDTO.password()
                )
            );
            
            Account account = accountService.getByEmail(loginDTO.email());
            String token = jwtUtil.generateToken(account.getEmail());
            
            AuthResponseDTO response = new AuthResponseDTO(
                token,
                account.getEmail(),
                account.getName(),
                account.getId()
            );
            
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordDTO dto) {
        try {
            // In a real application, you would send an email with reset instructions
            // For now, we'll just verify the email exists
            Account account = accountService.getByEmail(dto.email());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset instructions sent to " + dto.email());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Email not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            Account account = accountService.getByEmail(authentication.getName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", account.getId());
            response.put("name", account.getName());
            response.put("email", account.getEmail());
            response.put("createdAt", account.getCreatedAt());
            response.put("updatedAt", account.getUpdatedAt());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}