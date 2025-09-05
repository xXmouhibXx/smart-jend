package com.projectjend.demo.service.impl;

import com.projectjend.demo.dto.AccountDTO;
import com.projectjend.demo.dto.PasswordUpdateDTO;
import com.projectjend.demo.entity.Account;
import com.projectjend.demo.repository.AccountRepository;
import com.projectjend.demo.service.AccountService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;

  public AccountServiceImpl(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
    this.accountRepository = accountRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public Account createAccount(AccountDTO dto) {
    if (accountRepository.existsByEmail(dto.email())) {
      throw new IllegalArgumentException("Email already in use");
    }
    Account acc = new Account();
    acc.setName(dto.name());
    acc.setEmail(dto.email());
    acc.setPassword(passwordEncoder.encode(dto.password()));
    return accountRepository.save(acc);
  }

  @Override
  public Account updateAccount(Long id, AccountDTO dto) {
    Account acc = accountRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    acc.setName(dto.name());
    // email change allowed only if not used by someone else
    if (!acc.getEmail().equals(dto.email()) && accountRepository.existsByEmail(dto.email())) {
      throw new IllegalArgumentException("Email already in use");
    }
    acc.setEmail(dto.email());
    // update password only if non-blank (optional)
    if (dto.password() != null && !dto.password().isBlank()) {
      acc.setPassword(passwordEncoder.encode(dto.password()));
    }
    return accountRepository.save(acc);
  }

  @Override
  public void updatePassword(Long id, PasswordUpdateDTO dto) {
    Account acc = accountRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Account not found"));

    if (!passwordEncoder.matches(dto.oldPassword(), acc.getPassword())) {
      throw new IllegalArgumentException("Old password is incorrect");
    }
    acc.setPassword(passwordEncoder.encode(dto.newPassword()));
    accountRepository.save(acc);
  }

  @Override
  @Transactional(readOnly = true)
  public Account getByEmail(String email) {
    return accountRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("Account not found"));
  }
}
