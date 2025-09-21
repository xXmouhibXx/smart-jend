package com.projectjend.demo.controller;

import com.projectjend.demo.dto.ServiceProposalDTO;
import com.projectjend.demo.dto.ReviewDTO;
import com.projectjend.demo.entity.Account;
import com.projectjend.demo.entity.Review;
import com.projectjend.demo.entity.ServiceProposal;
import com.projectjend.demo.service.AccountService;
import com.projectjend.demo.service.ReviewService;
import com.projectjend.demo.service.ServiceProposalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/services")
@CrossOrigin(origins = "*")
public class ServiceProposalController {

    private final ServiceProposalService serviceProposalService;
    private final AccountService accountService;
    private final ReviewService reviewService;

    public ServiceProposalController(ServiceProposalService serviceProposalService, 
                                    AccountService accountService,
                                    ReviewService reviewService) {
        this.serviceProposalService = serviceProposalService;
        this.accountService = accountService;
        this.reviewService = reviewService;
    }

    // List all services (public)
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll() {
        List<ServiceProposal> services = serviceProposalService.findAll();
        
        List<Map<String, Object>> response = services.stream()
            .map(service -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", service.getId());
                map.put("name", service.getName());
                map.put("description", service.getDescription());
                map.put("location", service.getLocation());
                map.put("votes", service.getVotes());
                map.put("status", "active");
                
                // Add new fields
                map.put("ownerEmail", service.getOwnerEmail());
                map.put("endDate", service.getEndDate());
                map.put("reservationLink", service.getReservationLink());
                map.put("delegation", service.getDelegation());
                map.put("sector", service.getSector());
                map.put("provider", service.getProvider());
                map.put("institution", service.getInstitution());
                map.put("category", service.getCategory());
                map.put("averageRating", service.getAverageRating());
                map.put("reviewCount", service.getReviewCount());
                
                if (service.getProposedBy() != null) {
                    map.put("proposedById", service.getProposedBy().getId());
                }
                return map;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    // GET REVIEWS FOR A SERVICE - THIS IS THE CRITICAL ENDPOINT
    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<Map<String, Object>>> getServiceReviews(@PathVariable Long id) {
        System.out.println("=== FETCHING REVIEWS FOR SERVICE ID: " + id + " ===");
        
        try {
            List<Review> reviews = reviewService.getReviewsByServiceId(id);
            
            System.out.println("Found " + reviews.size() + " reviews in database");
            
            List<Map<String, Object>> response = reviews.stream()
                .map(review -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", review.getId());
                    
                    // Get client name from database or derive from email
                    String clientName = review.getClientName();
                    if (clientName == null || clientName.isEmpty()) {
                        clientName = review.getClientEmail();
                        if (clientName != null && clientName.contains("@")) {
                            clientName = clientName.substring(0, clientName.indexOf("@"));
                        }
                    }
                    map.put("clientName", clientName != null ? clientName : "مستخدم");
                    
                    map.put("clientEmail", review.getClientEmail());
                    
                    // Handle rating - convert BigDecimal to double
                    if (review.getRating() != null) {
                        map.put("rating", review.getRating().doubleValue());
                    } else {
                        map.put("rating", 0.0);
                    }
                    
                    // Handle comment
                    map.put("comment", review.getComment() != null ? review.getComment() : "");
                    
                    // Handle dates
                    if (review.getReviewDate() != null) {
                        map.put("reviewDate", review.getReviewDate().toString());
                    } else {
                        map.put("reviewDate", LocalDate.now().toString());
                    }
                    
                    if (review.getCreatedAt() != null) {
                        map.put("createdAt", review.getCreatedAt().toString());
                    }
                    
                    System.out.println("Review: " + clientName + " - Rating: " + map.get("rating") + " - Comment: " + map.get("comment"));
                    return map;
                })
                .collect(Collectors.toList());
        
            System.out.println("Returning " + response.size() + " reviews to client");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("ERROR fetching reviews: " + e.getMessage());
            e.printStackTrace();
            // Return empty list instead of error to prevent app crash
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // Create a service
    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody Map<String, Object> requestBody,
            Authentication auth) {
        try {
            Account user = accountService.getByEmail(auth.getName());
            
            String name = (String) requestBody.get("name");
            String description = (String) requestBody.get("description");
            String location = requestBody.containsKey("location") ? 
                (String) requestBody.get("location") : "36.81,10.17";
            
            ServiceProposalDTO dto = new ServiceProposalDTO(
                name,
                description,
                location,
                (String) requestBody.get("ownerEmail"),
                null,
                (String) requestBody.get("reservationLink"),
                (String) requestBody.get("delegation"),
                (String) requestBody.get("sector"),
                (String) requestBody.get("provider"),
                (String) requestBody.get("institution"),
                (String) requestBody.get("category")
            );
            
            ServiceProposal created = serviceProposalService.create(dto, user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", created.getId());
            response.put("name", created.getName());
            response.put("description", created.getDescription());
            response.put("location", created.getLocation());
            response.put("votes", created.getVotes());
            response.put("status", "pending");
            response.put("category", created.getCategory());
            response.put("institution", created.getInstitution());
            response.put("proposedById", user.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Update a service
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @Valid @RequestBody ServiceProposalDTO dto) {
        try {
            ServiceProposal updated = serviceProposalService.update(id, dto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", updated.getId());
            response.put("name", updated.getName());
            response.put("description", updated.getDescription());
            response.put("location", updated.getLocation());
            response.put("votes", updated.getVotes());
            response.put("category", updated.getCategory());
            response.put("institution", updated.getInstitution());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // Delete a service
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            serviceProposalService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Vote for a service
    @PostMapping("/{id}/vote")
    public ResponseEntity<?> vote(@PathVariable Long id) {
        try {
            ServiceProposal voted = serviceProposalService.vote(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", voted.getId());
            response.put("name", voted.getName());
            response.put("votes", voted.getVotes());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    // Add review
    @PostMapping("/{id}/review")
    public ResponseEntity<?> addReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewDTO reviewDTO,
            Authentication auth) {
        try {
            String userEmail = auth.getName();
            Account user = accountService.getByEmail(userEmail);
            
            ReviewDTO authenticatedReviewDTO = new ReviewDTO(
                userEmail,
                reviewDTO.provider(),
                id,
                reviewDTO.bookingStartDate(),
                reviewDTO.bookingEndDate(),
                reviewDTO.rating(),
                reviewDTO.comment()
            );
            
            Review review = reviewService.addReview(id, authenticatedReviewDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", review.getId());
            response.put("serviceId", id);
            response.put("clientName", review.getClientName());
            response.put("rating", review.getRating());
            response.put("comment", review.getComment());
            response.put("reviewDate", review.getReviewDate());
            response.put("message", "تم إضافة التقييم بنجاح");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "فشل إضافة التقييم: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
    
    // Check if user has reviewed
    @GetMapping("/{id}/has-reviewed")
    public ResponseEntity<?> hasUserReviewed(
            @PathVariable Long id,
            Authentication auth) {
        try {
            String userEmail = auth.getName();
            boolean hasReviewed = reviewService.hasUserReviewed(id, userEmail);
            
            Map<String, Object> response = new HashMap<>();
            response.put("hasReviewed", hasReviewed);
            response.put("userEmail", userEmail);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}