// ServiceProposal.java - Fixed version
package com.projectjend.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "service_proposals")
@Getter
@Setter
public class ServiceProposal extends BaseEntity {

  @NotBlank
  private String name;

  @NotBlank
  @Column(length = 2000)
  private String description;

  /**
   * Stored as "lat,lon" (e.g. "36.81,10.17")
   */
  @NotBlank
  private String location;

  private int votes = 0;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "proposed_by_id")
  private Account proposedBy;
  
  // New fields for service product registration
  @Column(name = "owner_email")
  private String ownerEmail;
  
  @Column(name = "end_date")
  private LocalDate endDate;
  
  @Column(name = "reservation_link", length = 500)
  private String reservationLink;
  
  private String delegation;
  
  private String sector;
  
  private String provider;
  
  private String institution;
  
  private String category;
  
  // FIX: Remove precision and scale for floating point
  @Column(name = "average_rating")
  private Double averageRating = 0.0;
  
  @Column(name = "review_count")
  private Integer reviewCount = 0;
}