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
      throw new IllegalArgumentException("البريد الإلكتروني مستخدم بالفعل");
    }
    Account acc = new Account();
    acc.setName(dto.name());
    acc.setEmail(dto.email());
    acc.setPassword(passwordEncoder.encode(dto.password()));
    
    // Set additional fields
    acc.setPhone(dto.phone());
    acc.setBirthDate(dto.birthDate());
    acc.setGender(dto.gender());
    acc.setDelegation(dto.delegation());
    acc.setSector(dto.sector());
    
    return accountRepository.save(acc);
  }

  @Override
  public Account updateAccount(Long id, AccountDTO dto) {
    Account acc = accountRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("الحساب غير موجود"));
    acc.setName(dto.name());
    // email change allowed only if not used by someone else
    if (!acc.getEmail().equals(dto.email()) && accountRepository.existsByEmail(dto.email())) {
      throw new IllegalArgumentException("البريد الإلكتروني مستخدم بالفعل");
    }
    acc.setEmail(dto.email());
    // update password only if non-blank (optional)
    if (dto.password() != null && !dto.password().isBlank()) {
      acc.setPassword(passwordEncoder.encode(dto.password()));
    }
    
    // Update additional fields
    acc.setPhone(dto.phone());
    acc.setBirthDate(dto.birthDate());
    acc.setGender(dto.gender());
    acc.setDelegation(dto.delegation());
    acc.setSector(dto.sector());
    
    return accountRepository.save(acc);
  }

  @Override
  public void updatePassword(Long id, PasswordUpdateDTO dto) {
    Account acc = accountRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("الحساب غير موجود"));

    if (!passwordEncoder.matches(dto.oldPassword(), acc.getPassword())) {
      throw new IllegalArgumentException("كلمة المرور القديمة غير صحيحة");
    }
    acc.setPassword(passwordEncoder.encode(dto.newPassword()));
    accountRepository.save(acc);
  }

  @Override
  @Transactional(readOnly = true)
  public Account getByEmail(String email) {
    return accountRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("الحساب غير موجود"));
  }
}