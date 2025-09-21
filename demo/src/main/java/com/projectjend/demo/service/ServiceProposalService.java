package com.projectjend.demo.service;

import com.projectjend.demo.dto.ServiceProposalDTO;
import com.projectjend.demo.dto.ReviewDTO;
import com.projectjend.demo.entity.Account;
import com.projectjend.demo.entity.ServiceProposal;

import java.util.List;

public interface ServiceProposalService {
  List<ServiceProposal> findAll();
  ServiceProposal create(ServiceProposalDTO dto, Account proposedBy);
  ServiceProposal update(Long id, ServiceProposalDTO dto);
  void delete(Long id);
  ServiceProposal vote(Long id);
  
  // ADD THIS METHOD SIGNATURE - This is what's missing!
  ServiceProposal addReview(Long id, ReviewDTO reviewDTO);
}