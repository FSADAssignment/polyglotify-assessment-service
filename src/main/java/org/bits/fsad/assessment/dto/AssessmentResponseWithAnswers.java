package org.bits.fsad.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentResponseWithAnswers {
    private Long attemptId;
    private String userId;
    private Long assessmentId;
    private List<QuestionWithCorrectAnswer> questionsWithAnswers;


}
