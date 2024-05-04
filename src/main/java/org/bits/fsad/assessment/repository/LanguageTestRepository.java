package org.bits.fsad.assessment.repository;

import org.bits.fsad.assessment.entity.LanguageTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LanguageTestRepository extends JpaRepository<LanguageTest, Long> {
    // Add custom query methods if needed
}
