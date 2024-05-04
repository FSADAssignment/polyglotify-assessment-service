package org.bits.fsad.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private String id;
    private String language;
    private String subcategory;
    private String level;
    private String questionText;
    private List<String> options;

}
