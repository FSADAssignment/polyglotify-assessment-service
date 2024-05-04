package org.bits.fsad.assessment.repository;

import org.bits.fsad.assessment.entity.UserTestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTestAttemptRepository extends JpaRepository<UserTestAttempt, Long> {
    boolean existsByUserIdAndAssessmentId(String userId, Long assessmentId);
}
