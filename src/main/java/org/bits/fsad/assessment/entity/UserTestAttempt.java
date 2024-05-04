package org.bits.fsad.assessment.entity;

import jakarta.persistence.*;
import lombok.Data;


import java.time.LocalDateTime;

@Entity
@Table(name = "user_test_attempts")
@Data
public class UserTestAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attemptId;

    @Column(name = "user_id", nullable = false) // Change to store only user ID
    private String userId;

    @Column(name = "assessment_id", nullable = false) // Change to store only assessment ID
    private Long assessmentId;

    private LocalDateTime attemptDate;

    @Column(nullable = false)
    private boolean attempted;


}
