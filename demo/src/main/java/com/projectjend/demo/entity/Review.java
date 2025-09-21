package com.projectjend.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "reviews")
@Getter
@Setter
public class Review extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_proposal_id", nullable = false)
    private ServiceProposal serviceProposal;
    
    @Column(name = "client_email", nullable = false)
    private String clientEmail;
    
    @Column(name = "client_name", nullable = false)
    private String clientName;
    
    // Add provider field that was missing
    @Column(name = "provider")
    private String provider;
    
    @DecimalMin("0.0")
    @DecimalMax("5.0")
    @Column(precision = 3, scale = 1, nullable = false)
    private BigDecimal rating;
    
    @Column(length = 1000)
    private String comment;
    
    @Column(name = "review_date")
    private LocalDate reviewDate;
    
    @Column(name = "booking_start_date")
    private LocalDate bookingStartDate;
    
    @Column(name = "booking_end_date")
    private LocalDate bookingEndDate;
}