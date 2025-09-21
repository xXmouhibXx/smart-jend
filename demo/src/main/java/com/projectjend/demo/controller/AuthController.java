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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AccountService accountService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    
    // Temporary storage for reset codes (in production, use database or Redis)
    private final Map<String, ResetCodeInfo> resetCodes = new ConcurrentHashMap<>();

    public AuthController(AccountService accountService, 
                         AuthenticationManager authenticationManager, 
                         JwtUtil jwtUtil,
                         PasswordEncoder passwordEncoder) {
        this.accountService = accountService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    // Inner class for storing reset code info
    private static class ResetCodeInfo {
        String email;
        String code;
        long timestamp;
        
        ResetCodeInfo(String email, String code) {
            this.email = email;
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired() {
            // Expire after 1 hour
            return System.currentTimeMillis() - timestamp > 3600000;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AccountDTO accountDTO) {
        try {
            Account account = accountService.createAccount(accountDTO);
            String token = jwtUtil.generateToken(account.getEmail());
            
            System.out.println("User registered: " + account.getEmail());
            System.out.println("Token generated: " + token.substring(0, 20) + "...");
            
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
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "فشل التسجيل: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Additional endpoint for client registration with all fields
    @PostMapping("/register/client")
    public ResponseEntity<?> registerClient(@Valid @RequestBody AccountDTO accountDTO) {
        try {
            // Register with all client fields
            Account account = accountService.createAccount(accountDTO);
            String token = jwtUtil.generateToken(account.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("email", account.getEmail());
            response.put("name", account.getName());
            response.put("id", account.getId());
            response.put("delegation", account.getDelegation());
            response.put("sector", account.getSector());
            response.put("message", "تم التسجيل بنجاح");
            
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
            System.out.println("Login attempt for: " + loginDTO.email());
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDTO.email(),
                    loginDTO.password()
                )
            );
            
            Account account = accountService.getByEmail(loginDTO.email());
            String token = jwtUtil.generateToken(account.getEmail());
            
            System.out.println("Login successful for: " + account.getEmail());
            System.out.println("Token generated: " + token.substring(0, 20) + "...");
            
            AuthResponseDTO response = new AuthResponseDTO(
                token,
                account.getEmail(),
                account.getName(),
                account.getId()
            );
            
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            System.err.println("Login failed for " + loginDTO.email() + ": " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "البريد الإلكتروني أو كلمة المرور غير صحيحة");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "فشل تسجيل الدخول: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordDTO dto) {
        try {
            // Verify the email exists
            Account account = accountService.getByEmail(dto.email());
            
            // Generate a 6-digit reset code
            String resetCode = String.format("%06d", (int) (Math.random() * 1000000));
            
            // Store the reset code with email
            resetCodes.put(resetCode, new ResetCodeInfo(dto.email(), resetCode));
            
            // Clean up expired codes
            resetCodes.entrySet().removeIf(entry -> entry.getValue().isExpired());
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "تم إرسال رمز إعادة تعيين كلمة المرور إلى " + dto.email());
            
            // In development/testing, return the code. Remove this in production!
            if ("development".equals(System.getProperty("environment", "development"))) {
                response.put("resetCode", resetCode);
            }
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "البريد الإلكتروني غير مسجل");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String resetCode = request.get("resetCode");
            String newPassword = request.get("newPassword");
            
            // Validate input
            if (email == null || resetCode == null || newPassword == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "جميع الحقول مطلوبة");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Verify reset code
            ResetCodeInfo codeInfo = resetCodes.get(resetCode);
            if (codeInfo == null || !codeInfo.email.equals(email) || codeInfo.isExpired()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "رمز إعادة التعيين غير صالح أو منتهي الصلاحية");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            // Update the password
            Account account = accountService.getByEmail(email);
            
            // Direct password update for reset
            accountService.updateAccount(account.getId(), new AccountDTO(
                account.getName(),
                account.getEmail(),
                newPassword,
                account.getPhone(),
                account.getBirthDate(),
                account.getGender(),
                account.getDelegation(),
                account.getSector()
            ));
            
            // Remove used reset code
            resetCodes.remove(resetCode);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "تم تغيير كلمة المرور بنجاح");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "فشل تغيير كلمة المرور");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            // Debug logging
            System.out.println("Profile request received");
            
            if (authentication == null) {
                System.err.println("No authentication object");
                Map<String, String> error = new HashMap<>();
                error.put("error", "غير مصرح - يجب تسجيل الدخول");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            if (!authentication.isAuthenticated()) {
                System.err.println("User not authenticated");
                Map<String, String> error = new HashMap<>();
                error.put("error", "غير مصرح - يجب تسجيل الدخول");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            String email = authentication.getName();
            System.out.println("Fetching profile for: " + email);
            
            Account account = accountService.getByEmail(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", account.getId());
            response.put("name", account.getName());
            response.put("email", account.getEmail());
            response.put("phone", account.getPhone());
            response.put("birthDate", account.getBirthDate());
            response.put("gender", account.getGender());
            response.put("delegation", account.getDelegation());
            response.put("sector", account.getSector());
            response.put("createdAt", account.getCreatedAt());
            response.put("updatedAt", account.getUpdatedAt());
            
            System.out.println("Profile returned successfully for: " + email);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            System.err.println("User not found: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "المستخدم غير موجود");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            System.err.println("Profile error: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "خطأ في الخادم: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Endpoint to verify if a reset code is valid (optional)
    @PostMapping("/verify-reset-code")
    public ResponseEntity<?> verifyResetCode(@RequestBody Map<String, String> request) {
        String resetCode = request.get("resetCode");
        String email = request.get("email");
        
        ResetCodeInfo codeInfo = resetCodes.get(resetCode);
        if (codeInfo != null && codeInfo.email.equals(email) && !codeInfo.isExpired()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "رمز إعادة التعيين صالح");
            response.put("valid", "true");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "رمز إعادة التعيين غير صالح أو منتهي الصلاحية");
            error.put("valid", "false");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}