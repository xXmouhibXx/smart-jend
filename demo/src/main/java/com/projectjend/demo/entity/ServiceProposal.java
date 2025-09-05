package com.projectjend.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

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
}
