package com.projectjend.demo.service.impl;

import com.projectjend.demo.dto.ServiceProposalDTO;
import com.projectjend.demo.dto.ReviewDTO;
import com.projectjend.demo.entity.Account;
import com.projectjend.demo.entity.ServiceProposal;
import com.projectjend.demo.repository.ServiceProposalRepository;
import com.projectjend.demo.service.ServiceProposalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ServiceProposalServiceImpl implements ServiceProposalService {

  private final ServiceProposalRepository repository;

  public ServiceProposalServiceImpl(ServiceProposalRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<ServiceProposal> findAll() {
    return repository.findAll();
  }

  @Override
  public ServiceProposal create(ServiceProposalDTO dto, Account proposedBy) {
    ServiceProposal sp = new ServiceProposal();
    sp.setName(dto.name());
    sp.setDescription(dto.description());
    sp.setLocation(dto.location());
    sp.setProposedBy(proposedBy);
    
    // Set additional fields for service product
    sp.setOwnerEmail(dto.ownerEmail() != null ? dto.ownerEmail() : proposedBy.getEmail());
    sp.setEndDate(dto.endDate());
    sp.setReservationLink(dto.reservationLink());
    sp.setDelegation(dto.delegation());
    sp.setSector(dto.sector());
    sp.setProvider(dto.provider());
    sp.setInstitution(dto.institution());
    sp.setCategory(dto.category());
    
    return repository.save(sp);
  }

  @Override
  public ServiceProposal update(Long id, ServiceProposalDTO dto) {
    ServiceProposal sp = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("الخدمة غير موجودة"));
    sp.setName(dto.name());
    sp.setDescription(dto.description());
    sp.setLocation(dto.location());
    
    // Update additional fields
    sp.setOwnerEmail(dto.ownerEmail());
    sp.setEndDate(dto.endDate());
    sp.setReservationLink(dto.reservationLink());
    sp.setDelegation(dto.delegation());
    sp.setSector(dto.sector());
    sp.setProvider(dto.provider());
    sp.setInstitution(dto.institution());
    sp.setCategory(dto.category());
    
    return repository.save(sp);
  }

  @Override
  public void delete(Long id) {
    if (!repository.existsById(id)) {
      throw new IllegalArgumentException("الخدمة غير موجودة");
    }
    repository.deleteById(id);
  }

  @Override
  public ServiceProposal vote(Long id) {
    ServiceProposal sp = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("الخدمة غير موجودة"));
    sp.setVotes(sp.getVotes() + 1);
    return repository.save(sp);
  }
  
  @Override  // ADD THIS ANNOTATION
  public ServiceProposal addReview(Long id, ReviewDTO reviewDTO) {
    ServiceProposal sp = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("الخدمة غير موجودة"));
    
    // Update average rating
    Integer currentCount = sp.getReviewCount() != null ? sp.getReviewCount() : 0;
    Double currentAvg = sp.getAverageRating() != null ? sp.getAverageRating() : 0.0;
    
    Double newAvg = ((currentAvg * currentCount) + reviewDTO.rating().doubleValue()) / (currentCount + 1);
    
    sp.setAverageRating(newAvg);
    sp.setReviewCount(currentCount + 1);
    
    return repository.save(sp);
  }
}