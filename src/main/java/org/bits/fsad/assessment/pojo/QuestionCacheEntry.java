package org.bits.fsad.assessment.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class QuestionCacheEntry {
    private String questionText;
    private List<String> options;
    private Integer correctOptionIndex;

    public QuestionCacheEntry(String questionText, List<String> options, Integer correctOptionIndex) {
        this.questionText = questionText;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
    }

    // Getters and setters
}
