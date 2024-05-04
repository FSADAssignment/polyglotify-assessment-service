package org.bits.fsad.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionWithCorrectAnswerDTO {
    private String questionId;
    private String questionText;
    private List<String> options;
    private int userSelectedOptionIndex;
    private int correctOptionIndex;
}