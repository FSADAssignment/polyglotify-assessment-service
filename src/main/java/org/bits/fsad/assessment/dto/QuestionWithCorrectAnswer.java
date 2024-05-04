package org.bits.fsad.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionWithCorrectAnswer {
    private String questionId;
    private int selectedOptionIndex;
    private Integer correctOptionIndex;


}
