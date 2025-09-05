package com.projectjend.demo.service;

import com.projectjend.demo.dto.AccountDTO;
import com.projectjend.demo.dto.PasswordUpdateDTO;
import com.projectjend.demo.entity.Account;

public interface AccountService {
  Account createAccount(AccountDTO dto);
  Account updateAccount(Long id, AccountDTO dto);
  void updatePassword(Long id, PasswordUpdateDTO dto);
  Account getByEmail(String email);
}
