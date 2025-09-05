package com.projectjend.demo.controller;

import com.projectjend.demo.dto.ServiceProposalDTO;
import com.projectjend.demo.entity.Account;
import com.projectjend.demo.entity.ServiceProposal;
import com.projectjend.demo.service.AccountService;
import com.projectjend.demo.service.ServiceProposalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    public ServiceProposalController(ServiceProposalService serviceProposalService, AccountService accountService) {
        this.serviceProposalService = serviceProposalService;
        this.accountService = accountService;
    }

    // List all services (public) - Modified to match Flutter expectations
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll() {
        List<ServiceProposal> services = serviceProposalService.findAll();
        
        // Convert to Flutter expected format
        List<Map<String, Object>> response = services.stream()
            .map(service -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", service.getId());
                map.put("name", service.getName());
                map.put("description", service.getDescription());
                map.put("location", service.getLocation());
                map.put("votes", service.getVotes());
                map.put("status", "active"); // Add status field expected by Flutter
                if (service.getProposedBy() != null) {
                    map.put("proposedById", service.getProposedBy().getId());
                }
                return map;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    // Propose a service (auth) - Modified to handle Flutter format
    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody Map<String, Object> requestBody,
            Authentication auth) {
        try {
            Account user = accountService.getByEmail(auth.getName());
            
            // Extract data from Flutter request
            String name = (String) requestBody.get("name");
            String description = (String) requestBody.get("description");
            
            // Handle location - Flutter might send coordinates or a location string
            String location = "36.81,10.17"; // Default to Jendouba coordinates
            if (requestBody.containsKey("location")) {
                location = (String) requestBody.get("location");
            }
            
            // Handle price if sent (Flutter sends this but Spring Boot doesn't use it)
            // Double price = requestBody.containsKey("price") ? 
            //     Double.parseDouble(requestBody.get("price").toString()) : 0.0;
            
            ServiceProposalDTO dto = new ServiceProposalDTO(name, description, location);
            ServiceProposal created = serviceProposalService.create(dto, user);
            
            // Return in Flutter expected format
            Map<String, Object> response = new HashMap<>();
            response.put("id", created.getId());
            response.put("name", created.getName());
            response.put("description", created.getDescription());
            response.put("location", created.getLocation());
            response.put("votes", created.getVotes());
            response.put("status", "pending");
            response.put("proposedById", user.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    // Update a service (auth)
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
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    // Delete a service (auth)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            serviceProposalService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Vote for a service (auth)
    @PostMapping("/{id}/vote")
    public ResponseEntity<?> vote(@PathVariable Long id) {
        try {
            ServiceProposal voted = serviceProposalService.vote(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", voted.getId());
            response.put("name", voted.getName());
            response.put("description", voted.getDescription());
            response.put("location", voted.getLocation());
            response.put("votes", voted.getVotes());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}