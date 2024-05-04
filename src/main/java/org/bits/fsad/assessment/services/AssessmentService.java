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
import org.bits.fsad.assessment.pojo.QuestionCacheEntry;
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

import java.io.IOException;
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

        return new QuestionWithAttemptId(questionDTOs, attemptId,assessment_id);


    }




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
                    String cacheEntry = createCacheEntryJson(question.getQuestionText(), question.getOptions(), correctOptionIndex);
                    questionCache.put(questionId, cacheEntry);
                }
            }
        }
    }

    private String createCacheEntryJson(String questionText, List<String> options, Integer correctOptionIndex) {
        try {
            QuestionCacheEntry cacheEntry = new QuestionCacheEntry(questionText, options, correctOptionIndex);
            return objectMapper.writeValueAsString(cacheEntry);
        } catch (JsonProcessingException e) {
            // Handle JSON processing exception
            e.printStackTrace();
            return null;
        }
    }


    public AssessmentResult calculateScore(AssessmentResponse assessmentResponse) throws JsonProcessingException {
        int score = 0;
        List<QuestionResponse> questionResponses = assessmentResponse.getAnswers();
        List<QuestionWithCorrectAnswerDTO> questionsWithAnswers = new ArrayList<>();

        for (QuestionResponse questionResponse : questionResponses) {
            String questionId = questionResponse.getQuestionId();
            int selectedOptionIndex = questionResponse.getSelectedOptionIndex();

            // Retrieve the correct option index and question details from the cache
            Cache questionCache = cacheManager.getCache("questionCache");
            String cacheEntryJson = questionCache != null ? questionCache.get(questionId, String.class) : null;

            if (cacheEntryJson != null) {
                try {
                    // Deserialize the cache entry JSON string into QuestionCacheEntry object
                    QuestionCacheEntry cacheEntry = objectMapper.readValue(cacheEntryJson, QuestionCacheEntry.class);

                    // Create a new object to hold question, selected option, and correct option
                    QuestionWithCorrectAnswerDTO questionWithAnswer = new QuestionWithCorrectAnswerDTO();
                    questionWithAnswer.setQuestionId(questionId);
                    questionWithAnswer.setQuestionText(cacheEntry.getQuestionText());
                    questionWithAnswer.setOptions(cacheEntry.getOptions());
                    questionWithAnswer.setUserSelectedOptionIndex(selectedOptionIndex);
                    questionWithAnswer.setCorrectOptionIndex(cacheEntry.getCorrectOptionIndex());

                    questionsWithAnswers.add(questionWithAnswer);

                    // If the correct option index is retrieved from the cache and matches the user's selected option index, increment the score
                    if (cacheEntry.getCorrectOptionIndex() != null && selectedOptionIndex == cacheEntry.getCorrectOptionIndex()) {
                        score++;
                    }
                } catch (IOException e) {
                    // Handle JSON processing exception
                    e.printStackTrace();
                }
            }
        }

        updateScore(assessmentResponse.getUserId(),assessmentResponse.getAssessmentId(),score);

        // Save assessment response with questions and correct answers
        AssessmentResult assessmentResult = new AssessmentResult();
        assessmentResult.setUserId(assessmentResponse.getUserId());
        assessmentResult.setAttemptId(assessmentResponse.getAttemptId());
        assessmentResult.setAssessmentId(assessmentResponse.getAssessmentId());
        assessmentResult.setScore(score);
        assessmentResult.setQuestions(questionsWithAnswers);

        saveAssessmentResponse(assessmentResult);

        return assessmentResult;
    }

    public void saveAssessmentResponse(AssessmentResult assessmentResult) throws JsonProcessingException {
        AssessmentResponseEntity entity = new AssessmentResponseEntity();
        entity.setAttemptId(assessmentResult.getAttemptId());
        entity.setUserId(assessmentResult.getUserId());
        entity.setAssessmentId(assessmentResult.getAssessmentId());

        entity.setResponseData(objectMapper.writeValueAsString(assessmentResult.getQuestions()));
        entity.setCreatedAt(LocalDateTime.now());
        assessmentResponseRepository.save(entity);
    }

    public void updateScore(String userId, Long assessmentId, int score) {

        Score scoreEntity = new Score();
        scoreEntity.setUserId(userId);
        scoreEntity.setAssessmentId(assessmentId);
        scoreEntity.setScore(score);

       scoreRepository.save(scoreEntity);
    }
}