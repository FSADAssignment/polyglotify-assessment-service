package org.bits.fsad.assessment.repository;

import org.bits.fsad.assessment.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    List<Assessment> findByLanguage(String language);
    Assessment findByLanguageAndTitleAndDifficultyLevel(String language, String title, String difficultyLevel);
}
