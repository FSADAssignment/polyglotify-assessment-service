package org.bits.fsad.assessment.dto;

import lombok.Data;
import org.bits.fsad.assessment.pojo.Question;


import java.util.List;


@Data
public class QuestionWithAttemptId {
    private List<Question> questions;
    private Long attemptId;

    public QuestionWithAttemptId(List<Question> questions, Long attemptId) {
        this.questions = questions;
        this.attemptId = attemptId;
    }
}