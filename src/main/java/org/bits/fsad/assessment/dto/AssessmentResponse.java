package org.bits.fsad.assessment.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssessmentResponse {
    private Long attemptId;
    private List<QuestionResponse> answers;
}
