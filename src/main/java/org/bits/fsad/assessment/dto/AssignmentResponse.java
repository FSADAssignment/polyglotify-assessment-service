package org.bits.fsad.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bits.fsad.assessment.entity.Assessment;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {

    private Long id;
    private String title;
    private String description;
    private String language;
    private String difficultyLevel;
    private boolean attempted;

    public AssignmentResponse(Assessment assessment, boolean attempted) {
        this.id = assessment.getAssessmentId();
        this.title = assessment.getTitle();
        this.description = assessment.getDescription();
        this.language = assessment.getLanguage();
        this.difficultyLevel = assessment.getDifficultyLevel();
        this.attempted = attempted;
    }

    // Constructors, getters, and setters
}
