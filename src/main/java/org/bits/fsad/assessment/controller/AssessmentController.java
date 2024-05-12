package org.bits.fsad.assessment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.bits.fsad.assessment.dto.*;
import org.bits.fsad.assessment.pojo.Question;
import org.bits.fsad.assessment.services.AssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
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
    public ResponseEntity<AssessmentResult> submitAssessment(@RequestBody AssessmentResponse assessmentResponse) throws JsonProcessingException {
        AssessmentResult rsponse = assessmentService.calculateScore(assessmentResponse);
        return ResponseEntity.ok(rsponse);
    }

    @GetMapping("/progress")
        public ResponseEntity<Double> progress(@RequestParam String username,
                                                         @RequestParam String language) throws JsonProcessingException {
        double rsponse = assessmentService.calculateProgress(username, language);
        return ResponseEntity.ok(rsponse);
    }

}
