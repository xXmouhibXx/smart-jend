package com.projectjend.demo.controller;

import com.projectjend.demo.dto.AccountDTO;
import com.projectjend.demo.dto.PasswordUpdateDTO;
import com.projectjend.demo.entity.Account;
import com.projectjend.demo.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

  private final AccountService accountService;

  public AccountController(AccountService accountService) {
    this.accountService = accountService;
  }

  // Register (public)
  @PostMapping
  public ResponseEntity<Account> createAccount(@Valid @RequestBody AccountDTO accountDTO) {
    return ResponseEntity.ok(accountService.createAccount(accountDTO));
  }

  // Update account (auth required)
  @PutMapping("/{id}")
  public ResponseEntity<Account> updateAccount(
      @PathVariable Long id,
      @Valid @RequestBody AccountDTO accountDTO) {
    return ResponseEntity.ok(accountService.updateAccount(id, accountDTO));
  }

  // Change password (auth required)
  @PutMapping("/{id}/password")
  public ResponseEntity<Void> updatePassword(
      @PathVariable Long id,
      @Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
    accountService.updatePassword(id, passwordUpdateDTO);
    return ResponseEntity.ok().build();
  }

  // Verify current user / profile (auth required)
  @GetMapping("/me")
  public ResponseEntity<Account> me(Authentication auth) {
    Account acc = accountService.getByEmail(auth.getName());
    // Hide password in response
    acc.setPassword("********");
    return ResponseEntity.ok(acc);
  }
}
