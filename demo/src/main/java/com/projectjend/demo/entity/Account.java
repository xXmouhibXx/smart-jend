package com.projectjend.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "accounts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"email"})
})
@Getter
@Setter
public class Account extends BaseEntity {

  @NotBlank
  private String name;

  @Email
  @NotBlank
  private String email;

  @NotBlank
  private String password;
  
  // New fields for client registration
  private String phone;
  
  @Column(name = "birth_date")
  private LocalDate birthDate;
  
  private String gender;
  
  private String delegation;
  
  private String sector;
}