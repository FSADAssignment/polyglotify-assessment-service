package org.bits.fsad.assessment.dto;

import lombok.Data;

@Data
public class QuestionResponse {
    private String questionId;
    private int selectedOptionIndex;
}
