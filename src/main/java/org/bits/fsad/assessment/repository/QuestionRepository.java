package org.bits.fsad.assessment.repository;

import org.bits.fsad.assessment.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    // Add custom query methods if needed
}
