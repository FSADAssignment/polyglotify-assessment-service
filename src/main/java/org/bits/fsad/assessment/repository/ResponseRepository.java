package org.bits.fsad.assessment.repository;

import org.bits.fsad.assessment.entity.Response;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponseRepository extends JpaRepository<Response, Long> {
    // Add custom query methods if needed
}
