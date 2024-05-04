package org.bits.fsad.assessment.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;
@Data
public class Question {
    private String id;
    private String language;
    private String subcategory;
    private String level;
    private String questionText;
    private List<String> options;
    @JsonIgnore
    private Integer correctOptionIndex;

}
