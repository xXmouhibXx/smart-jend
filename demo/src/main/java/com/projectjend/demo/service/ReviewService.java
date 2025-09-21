package com.projectjend.demo.service;

import com.projectjend.demo.dto.ReviewDTO;
import com.projectjend.demo.entity.Review;

import java.util.List;

public interface ReviewService {
    Review addReview(Long serviceProposalId, ReviewDTO reviewDTO);
    List<Review> getReviewsByServiceId(Long serviceProposalId);
    List<Review> getReviewsByClientEmail(String clientEmail);
    void deleteReview(Long reviewId);
    boolean hasUserReviewed(Long serviceProposalId, String clientEmail);
}