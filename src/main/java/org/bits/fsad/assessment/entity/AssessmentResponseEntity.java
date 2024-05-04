package org.bits.fsad.assessment.entity;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;


import java.time.LocalDateTime;

@Entity
@Table(name = "assessment_response")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssessmentResponseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attempt_id")
    private Long attemptId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "assessment_id")
    private Long assessmentId;

    @Column(name = "response_data", columnDefinition = "jsonb")
    @Type(JsonType.class)
    private String responseData;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Getters and setters
}
