package com.projectjend.demo.repository;

import com.projectjend.demo.entity.ServiceProposal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceProposalRepository extends JpaRepository<ServiceProposal, Long> {
}
