package org.bits.fsad.assessment.services;

import org.bits.fsad.assessment.dto.AssessmentRequest;
import org.bits.fsad.assessment.dto.AssignmentResponse;
import org.bits.fsad.assessment.dto.QuestionWithAttemptId;
import org.bits.fsad.assessment.entity.Assessment;
import org.bits.fsad.assessment.entity.UserTestAttempt;
import org.bits.fsad.assessment.pojo.Question;
import org.bits.fsad.assessment.repository.AssessmentRepository;
import org.bits.fsad.assessment.repository.UserTestAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssessmentService {

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private UserTestAttemptRepository userTestAttemptRepository;

    @Autowired
    private RestTemplate restTemplate; // Used to make HTTP calls to the quiz service

//    @Autowired
//    private RedisTemplate<String, List<Question>> redisTemplate; // For interacting with Redis

    public QuestionWithAttemptId fetchQuestions(AssessmentRequest request) {
        String baseurl = "http://localhost:8888/quiz/questions/";
        String language = request.getLanguage();
        String subcategory = request.getSubcategory();
        String level = request.getLevel();

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseurl).path(language)
                .queryParam("subcategory", subcategory)
                .queryParam("level", level);

        ResponseEntity<List<Question>> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Question>>() {
                }
        );

        List<Question> questions = response.getBody();
        Long assessment_id = determineAssessmentId(language, subcategory, level);
        Long attemptId=markTestAsAttempted(request.getUsername(), assessment_id);


        return new QuestionWithAttemptId(questions, attemptId);


    }

//    private String generateRedisKey(AssessmentRequest request) {
////        return String.format("%s_%s_%s_%s", request.getUsername(), request.getLanguage(),
////                request.getSubcategory(), request.getLevel());
//    }


    public List<AssignmentResponse> getAvailableAssignmentsForLanguageAndUser(String language, String userId) {
        List<Assessment> availableAssignments = assessmentRepository.findByLanguage(language);

        return availableAssignments.stream()
                .map(assessment -> {
                    boolean attempted = isAssignmentAttempted(userId, assessment.getAssessmentId());
                    return new AssignmentResponse(assessment, attempted);
                })
                .collect(Collectors.toList());
    }

    public boolean isAssignmentAttempted(String userId, Long assessmentId) {
        return userTestAttemptRepository.existsByUserIdAndAssessmentId(userId, assessmentId);
    }


    public Long determineAssessmentId(String language, String title, String difficultyLevel) {
        Assessment assessment = assessmentRepository.findByLanguageAndTitleAndDifficultyLevel(language, title, difficultyLevel);
        if (assessment != null) {
            return assessment.getAssessmentId();
        } else {
            // TO DO: to handle if assessment is not found
            return null;
        }
    }

    public Long markTestAsAttempted(String userId, Long assessmentId) {
        UserTestAttempt userTestAttempt = new UserTestAttempt();
        userTestAttempt.setUserId(userId);
        userTestAttempt.setAssessmentId(assessmentId);
        userTestAttempt.setAttemptDate(LocalDateTime.now());
        userTestAttempt.setAttempted(true);
        userTestAttempt = userTestAttemptRepository.save(userTestAttempt);
        return userTestAttempt.getAttemptId(); // Return the generated attemptId
    }


}
