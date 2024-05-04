package org.bits.fsad.assessment.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "responses")
public class Response {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long responseId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    private String responseText;

    private Boolean isCorrect;

    // Getters and setters
}
