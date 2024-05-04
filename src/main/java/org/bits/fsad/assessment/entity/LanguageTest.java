package org.bits.fsad.assessment.entity;

import jakarta.persistence.*;


@Entity
@Table(name = "language_tests")
public class LanguageTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long testId;

    private String language;

    @ManyToOne
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    // Add constructors, getters, and setters
}
