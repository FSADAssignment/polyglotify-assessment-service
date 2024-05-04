package org.bits.fsad.assessment.dto;

import lombok.Data;


import java.util.List;


@Data
public class QuestionWithAttemptId {
    private List<QuestionDTO> questions;
    private Long attemptId;
    private Long assessmentId;

    public QuestionWithAttemptId(List<QuestionDTO> questions, Long attemptId, Long assessmentId) {
        this.questions = questions;
        this.attemptId = attemptId;
        this.assessmentId=assessmentId;
    }
}