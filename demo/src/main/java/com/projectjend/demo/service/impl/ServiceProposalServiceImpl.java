package com.projectjend.demo.service.impl;

import com.projectjend.demo.dto.ServiceProposalDTO;
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
    return repository.save(sp);
  }

  @Override
  public ServiceProposal update(Long id, ServiceProposalDTO dto) {
    ServiceProposal sp = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Service not found"));
    sp.setName(dto.name());
    sp.setDescription(dto.description());
    sp.setLocation(dto.location());
    return repository.save(sp);
  }

  @Override
  public void delete(Long id) {
    if (!repository.existsById(id)) {
      throw new IllegalArgumentException("Service not found");
    }
    repository.deleteById(id);
  }

  @Override
  public ServiceProposal vote(Long id) {
    ServiceProposal sp = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Service not found"));
    sp.setVotes(sp.getVotes() + 1);
    return repository.save(sp);
  }
}
