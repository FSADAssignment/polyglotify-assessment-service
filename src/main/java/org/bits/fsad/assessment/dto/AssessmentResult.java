package org.bits.fsad.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentResult {
    private String userId;
    private Long attemptId;
    private Long assessmentId;
    private int score;
    private List<QuestionWithCorrectAnswerDTO> questions;
}
