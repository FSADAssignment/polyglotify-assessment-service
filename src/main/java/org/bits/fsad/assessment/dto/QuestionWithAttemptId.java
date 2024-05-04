package org.bits.fsad.assessment.dto;

import lombok.Data;


import java.util.List;


@Data
public class QuestionWithAttemptId {
    private List<QuestionDTO> questions;
    private Long attemptId;

    public QuestionWithAttemptId(List<QuestionDTO> questions, Long attemptId) {
        this.questions = questions;
        this.attemptId = attemptId;
    }
}