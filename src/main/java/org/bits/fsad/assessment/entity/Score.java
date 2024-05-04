package org.bits.fsad.assessment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "scores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scoreId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "assessment_id", nullable = false)
    private Long assessmentId;

    private Integer score;


}
