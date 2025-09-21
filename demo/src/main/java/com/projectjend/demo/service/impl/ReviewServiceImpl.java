package com.projectjend.demo.service.impl;

import com.projectjend.demo.dto.ReviewDTO;
import com.projectjend.demo.entity.Account;
import com.projectjend.demo.entity.Review;
import com.projectjend.demo.entity.ServiceProposal;
import com.projectjend.demo.repository.AccountRepository;
import com.projectjend.demo.repository.ReviewRepository;
import com.projectjend.demo.repository.ServiceProposalRepository;
import com.projectjend.demo.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final ServiceProposalRepository serviceProposalRepository;
    private final AccountRepository accountRepository;
    
    public ReviewServiceImpl(ReviewRepository reviewRepository, 
                           ServiceProposalRepository serviceProposalRepository,
                           AccountRepository accountRepository) {
        this.reviewRepository = reviewRepository;
        this.serviceProposalRepository = serviceProposalRepository;
        this.accountRepository = accountRepository;
    }
    
    @Override
    public Review addReview(Long serviceProposalId, ReviewDTO reviewDTO) {
        // Check if user already reviewed this service
        if (hasUserReviewed(serviceProposalId, reviewDTO.clientEmail())) {
            throw new IllegalArgumentException("لقد قمت بتقييم هذه الخدمة مسبقاً");
        }
        
        ServiceProposal serviceProposal = serviceProposalRepository.findById(serviceProposalId)
            .orElseThrow(() -> new IllegalArgumentException("الخدمة غير موجودة"));
        
        // Get client name from account if exists
        String clientName = reviewDTO.clientEmail();
        Account account = accountRepository.findByEmail(reviewDTO.clientEmail()).orElse(null);
        if (account != null && account.getName() != null) {
            clientName = account.getName();
        } else if (clientName != null && clientName.contains("@")) {
            // Use email prefix as name
            clientName = clientName.substring(0, clientName.indexOf("@"));
        }
        
        // Create new review
        Review review = new Review();
        review.setServiceProposal(serviceProposal);
        review.setClientEmail(reviewDTO.clientEmail());
        review.setClientName(clientName);
        review.setProvider(reviewDTO.provider() != null ? reviewDTO.provider() : serviceProposal.getName());
        review.setRating(reviewDTO.rating());
        review.setComment(reviewDTO.comment() != null ? reviewDTO.comment() : "");
        review.setReviewDate(LocalDate.now());
        
        // Set booking dates
        if (reviewDTO.bookingStartDate() != null) {
            review.setBookingStartDate(reviewDTO.bookingStartDate());
        } else {
            review.setBookingStartDate(LocalDate.now().minusDays(7));
        }
        
        if (reviewDTO.bookingEndDate() != null) {
            review.setBookingEndDate(reviewDTO.bookingEndDate());
        } else {
            review.setBookingEndDate(LocalDate.now());
        }
        
        Review savedReview = reviewRepository.save(review);
        
        // Update service proposal average rating
        updateServiceRating(serviceProposal);
        
        System.out.println("Review saved: ID=" + savedReview.getId() + 
                         ", Client=" + savedReview.getClientName() + 
                         ", Rating=" + savedReview.getRating() + 
                         ", Comment=" + savedReview.getComment());
        
        return savedReview;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Review> getReviewsByServiceId(Long serviceProposalId) {
        System.out.println("=== ReviewService: Fetching reviews for service ID: " + serviceProposalId + " ===");
        
        List<Review> reviews = reviewRepository.findByServiceProposalIdOrderByCreatedAtDesc(serviceProposalId);
        
        System.out.println("ReviewService: Found " + reviews.size() + " reviews in database");
        
        // Log each review for debugging
        for (Review review : reviews) {
            System.out.println("Review: ID=" + review.getId() + 
                             ", Client=" + review.getClientName() + 
                             ", Email=" + review.getClientEmail() + 
                             ", Rating=" + review.getRating() + 
                             ", Comment=" + review.getComment() + 
                             ", ReviewDate=" + review.getReviewDate());
        }
        
        return reviews;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Review> getReviewsByClientEmail(String clientEmail) {
        return reviewRepository.findByClientEmailOrderByCreatedAtDesc(clientEmail);
    }
    
    @Override
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("التقييم غير موجود"));
        
        ServiceProposal serviceProposal = review.getServiceProposal();
        reviewRepository.deleteById(reviewId);
        
        // Update service rating after deletion
        updateServiceRating(serviceProposal);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReviewed(Long serviceProposalId, String clientEmail) {
        boolean hasReviewed = reviewRepository.existsByServiceProposalIdAndClientEmail(serviceProposalId, clientEmail);
        System.out.println("User " + clientEmail + " has reviewed service " + serviceProposalId + ": " + hasReviewed);
        return hasReviewed;
    }
    
    private void updateServiceRating(ServiceProposal serviceProposal) {
        List<Review> reviews = reviewRepository.findByServiceProposalIdOrderByCreatedAtDesc(serviceProposal.getId());
        
        if (reviews.isEmpty()) {
            serviceProposal.setAverageRating(0.0);
            serviceProposal.setReviewCount(0);
        } else {
            BigDecimal sum = reviews.stream()
                .map(Review::getRating)
                .filter(rating -> rating != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            if (reviews.size() > 0) {
                BigDecimal average = sum.divide(BigDecimal.valueOf(reviews.size()), 1, RoundingMode.HALF_UP);
                serviceProposal.setAverageRating(average.doubleValue());
            } else {
                serviceProposal.setAverageRating(0.0);
            }
            
            serviceProposal.setReviewCount(reviews.size());
        }
        
        serviceProposalRepository.save(serviceProposal);
        
        System.out.println("Updated service " + serviceProposal.getId() + 
                         " - Average Rating: " + serviceProposal.getAverageRating() + 
                         " - Review Count: " + serviceProposal.getReviewCount());
    }
}