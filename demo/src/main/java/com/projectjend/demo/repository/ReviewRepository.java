package com.projectjend.demo.repository;

import com.projectjend.demo.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    // Primary method to get reviews for a service
    @Query("SELECT r FROM Review r WHERE r.serviceProposal.id = :serviceProposalId ORDER BY r.createdAt DESC")
    List<Review> findByServiceProposalIdOrderByCreatedAtDesc(@Param("serviceProposalId") Long serviceProposalId);
    
    // Get reviews by client email
    @Query("SELECT r FROM Review r WHERE r.clientEmail = :clientEmail ORDER BY r.createdAt DESC")
    List<Review> findByClientEmailOrderByCreatedAtDesc(@Param("clientEmail") String clientEmail);
    
    // Alternative method with explicit join
    @Query("SELECT r FROM Review r JOIN r.serviceProposal sp WHERE sp.id = :serviceId ORDER BY r.createdAt DESC")
    List<Review> findReviewsByServiceId(@Param("serviceId") Long serviceId);
    
    // Check if user has reviewed
    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.serviceProposal.id = :serviceProposalId AND r.clientEmail = :clientEmail")
    boolean existsByServiceProposalIdAndClientEmail(@Param("serviceProposalId") Long serviceProposalId, 
                                                   @Param("clientEmail") String clientEmail);
}