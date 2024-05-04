package org.bits.fsad.assessment.controller;

import org.bits.fsad.assessment.dto.AssessmentRequest;
import org.bits.fsad.assessment.dto.AssessmentResponse;
import org.bits.fsad.assessment.dto.AssignmentResponse;
import org.bits.fsad.assessment.dto.QuestionWithAttemptId;
import org.bits.fsad.assessment.pojo.Question;
import org.bits.fsad.assessment.services.AssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/assessment")
public class AssessmentController {

    @Autowired
    private AssessmentService assessmentService;

    @GetMapping
    public ResponseEntity<QuestionWithAttemptId> fetchQuestions(
            @RequestParam String username,
            @RequestParam String language,
            @RequestParam String subcategory,
            @RequestParam String level) {
        AssessmentRequest request = new AssessmentRequest();
        request.setUsername(username);
        request.setLanguage(language);
        request.setSubcategory(subcategory);
        request.setLevel(level);

        QuestionWithAttemptId questions = assessmentService.fetchQuestions(request);
        return ResponseEntity.ok(questions);
    }


    @GetMapping("/available")
    public ResponseEntity<List<AssignmentResponse>> getAvailableAssignments(
            @RequestParam String language,
            @RequestParam String userId) {
        List<AssignmentResponse> availableAssignments = assessmentService.getAvailableAssignmentsForLanguageAndUser(language, userId);
        return ResponseEntity.ok(availableAssignments);
    }


    @PostMapping
    public ResponseEntity<String> submitAssessment(@RequestBody AssessmentResponse assessmentResponse) {
        // Here you can handle the assessment submission logic
        return ResponseEntity.ok("Assessment submitted successfully");
    }

}
