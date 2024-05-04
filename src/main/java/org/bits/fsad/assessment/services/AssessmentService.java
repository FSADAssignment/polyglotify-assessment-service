package org.bits.fsad.assessment.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.bits.fsad.assessment.dto.*;
import org.bits.fsad.assessment.entity.Assessment;
import org.bits.fsad.assessment.entity.AssessmentResponseEntity;
import org.bits.fsad.assessment.entity.Score;
import org.bits.fsad.assessment.entity.UserTestAttempt;
import org.bits.fsad.assessment.pojo.Question;
import org.bits.fsad.assessment.repository.AssessmentRepository;
import org.bits.fsad.assessment.repository.AssessmentResponseRepository;
import org.bits.fsad.assessment.repository.ScoreRepository;
import org.bits.fsad.assessment.repository.UserTestAttemptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AssessmentService {

    @Autowired
    private CacheManager cacheManager;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private UserTestAttemptRepository userTestAttemptRepository;

    @Autowired
    private RestTemplate restTemplate; // Used to make HTTP calls to the quiz service

    @Autowired
    private AssessmentResponseRepository assessmentResponseRepository;


    @Autowired
    ScoreRepository scoreRepository;

    @Autowired
    ObjectMapper objectMapper;


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
        Long attemptId = markTestAsAttempted(request.getUsername(), assessment_id);

        updateCache(questions);
        List<QuestionDTO> questionDTOs = new ArrayList<>();
        for (Question question : questions) {
            QuestionDTO questionDTO = new QuestionDTO();
            questionDTO.setId(question.getId());
            questionDTO.setLanguage(question.getLanguage());
            questionDTO.setSubcategory(question.getSubcategory());
            questionDTO.setLevel(question.getLevel());
            questionDTO.setQuestionText(question.getQuestionText());
            questionDTO.setOptions(question.getOptions());
            questionDTOs.add(questionDTO);
        }

        return new QuestionWithAttemptId(questionDTOs, attemptId);


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


    private void updateCache(List<Question> questions) {
        Cache questionCache = cacheManager.getCache("questionCache");
        if (questionCache != null) {
            for (Question question : questions) {
                String questionId = question.getId();
                Integer correctOptionIndex = question.getCorrectOptionIndex();
                if (questionId != null && correctOptionIndex != null) {
                    questionCache.put(questionId, correctOptionIndex);
                }
            }
        }
    }


    public int calculateScore(AssessmentResponse assessmentResponse) throws JsonProcessingException {
        int score = 0;
        List<QuestionResponse> questionResponses = assessmentResponse.getAnswers();
        List<QuestionWithCorrectAnswer> questionsWithAnswers = new ArrayList<>();

        for (QuestionResponse questionResponse : questionResponses) {
            String questionId = questionResponse.getQuestionId();
            int selectedOptionIndex = questionResponse.getSelectedOptionIndex();

            // Retrieve the correct option index from the cache
            Cache questionCache = cacheManager.getCache("questionCache");
            Integer correctOptionIndex = questionCache != null ? questionCache.get(questionId, Integer.class) : null;

            // Create a new object to hold question, selected option, and correct option
            QuestionWithCorrectAnswer questionWithAnswer = new QuestionWithCorrectAnswer();
            questionWithAnswer.setQuestionId(questionId);
            questionWithAnswer.setSelectedOptionIndex(selectedOptionIndex);
            questionWithAnswer.setCorrectOptionIndex(correctOptionIndex);

            questionsWithAnswers.add(questionWithAnswer);

            // If the correct option index is retrieved from the cache and matches the user's selected option index, increment the score
            if (correctOptionIndex != null && selectedOptionIndex == correctOptionIndex) {
                score++;
            }
        }

        // Save assessment response with questions and correct answers
        AssessmentResponseWithAnswers responseWithAnswers = new AssessmentResponseWithAnswers();
        responseWithAnswers.setAttemptId(assessmentResponse.getAttemptId());
        responseWithAnswers.setUserId(assessmentResponse.getUserId());
        responseWithAnswers.setAssessmentId(assessmentResponse.getAssessmentId());
        responseWithAnswers.setQuestionsWithAnswers(questionsWithAnswers);

        saveAssessmentResponse(responseWithAnswers);
        updateScore(assessmentResponse.getUserId(),assessmentResponse.getAssessmentId(),score);

        return score;
    }

    public void saveAssessmentResponse(AssessmentResponseWithAnswers assessmentResponseWithAnswers) throws JsonProcessingException {
        AssessmentResponseEntity entity = new AssessmentResponseEntity();
        entity.setAttemptId(assessmentResponseWithAnswers.getAttemptId());
        entity.setUserId(assessmentResponseWithAnswers.getUserId());
        entity.setAssessmentId(assessmentResponseWithAnswers.getAssessmentId());

        entity.setResponseData(objectMapper.writeValueAsString(assessmentResponseWithAnswers.getQuestionsWithAnswers()));
        entity.setCreatedAt(LocalDateTime.now());
        assessmentResponseRepository.save(entity);
    }

    public void updateScore(String userId, Long assessmentId, int score) {

        Score scoreEntity = new Score();
        scoreEntity.setUserId(userId);
        scoreEntity.setAssessmentId(assessmentId);
        scoreEntity.setScore(score);


        // Save the updated or new score entry
        scoreRepository.save(scoreEntity);
    }


}
