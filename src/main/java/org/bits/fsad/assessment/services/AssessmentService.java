package org.bits.fsad.assessment.services;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.Set;
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

    /**
     * Fetch questions for an assessment based on language, subcategory, and level.
     *
     * @param request The AssessmentRequest object containing language, subcategory, level, and username.
     * @return QuestionWithAttemptId object containing questions, attemptId, and assessmentId.
     */
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
                new ParameterizedTypeReference<List<Question>>() {}
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

        return new QuestionWithAttemptId(questionDTOs, attemptId, assessment_id);
    }

    /**
     * Get available assignments for a specific language and user.
     *
     * @param language The language for which assignments are to be fetched.
     * @param userId   The user for whom assignments are to be fetched.
     * @return List of AssignmentResponse objects containing assignment details.
     */
    public List<AssignmentResponse> getAvailableAssignmentsForLanguageAndUser(String language, String userId) {
        List<Assessment> availableAssignments = assessmentRepository.findByLanguage(language);

        return availableAssignments.stream()
                .map(assessment -> {
                    boolean attempted = isAssignmentAttempted(userId, assessment.getAssessmentId());
                    return new AssignmentResponse(assessment, attempted);
                })
                .collect(Collectors.toList());
    }

    /**
     * Check if a specific assignment is attempted by a user.
     *
     * @param userId       The user for whom assignment is checked.
     * @param assessmentId The ID of the assessment to check.
     * @return true if assignment is attempted, false otherwise.
     */
    public boolean isAssignmentAttempted(String userId, Long assessmentId) {
        return userTestAttemptRepository.existsByUserIdAndAssessmentId(userId, assessmentId);
    }

    /**
     * Determine the assessment ID based on language, title, and difficulty level.
     *
     * @param language        The language of the assessment.
     * @param title           The title of the assessment.
     * @param difficultyLevel The difficulty level of the assessment.
     * @return The ID of the assessment if found, otherwise null.
     */
    public Long determineAssessmentId(String language, String title, String difficultyLevel) {
        Assessment assessment = assessmentRepository.findByLanguageAndTitleAndDifficultyLevel(language, title, difficultyLevel);
        if (assessment != null) {
            return assessment.getAssessmentId();
        } else {
            // TO DO: to handle if assessment is not found
            return null;
        }
    }

    /**
     * Mark a test as attempted by a user.
     *
     * @param userId       The user who attempted the test.
     * @param assessmentId The ID of the assessment attempted.
     * @return The generated attempt ID.
     */
    public Long markTestAsAttempted(String userId, Long assessmentId) {
        UserTestAttempt userTestAttempt = new UserTestAttempt();
        userTestAttempt.setUserId(userId);
        userTestAttempt.setAssessmentId(assessmentId);
        userTestAttempt.setAttemptDate(LocalDateTime.now());
        userTestAttempt.setAttempted(true);
        userTestAttempt = userTestAttemptRepository.save(userTestAttempt);
        return userTestAttempt.getAttemptId(); // Return the generated attemptId
    }

    /**
     * Update the cache with questions.
     *
     * @param questions The list of questions to update in the cache.
     */
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

    /**
     * Create a cache entry JSON string.
     *
     * @param questionText      The text of the question.
     * @param options           The list of options for the question.
     * @param correctOptionIndex The index of the correct option.
     * @return The JSON string representing the cache entry.
     */
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

    /**
     * Calculate the score based on the assessment response.
     *
     * @param assessmentResponse The AssessmentResponse object containing user's responses.
     * @return The AssessmentResult object containing user's score and responses.
     * @throws JsonProcessingException If there's an error processing JSON.
     */
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

        updateScore(assessmentResponse.getUserId(), assessmentResponse.getAssessmentId(), score);

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

    /**
     * Save assessment response to the database.
     *
     * @param assessmentResult The AssessmentResult object to be saved.
     * @throws JsonProcessingException If there's an error processing JSON.
     */
    public void saveAssessmentResponse(AssessmentResult assessmentResult) throws JsonProcessingException {
        AssessmentResponseEntity entity = new AssessmentResponseEntity();
        entity.setAttemptId(assessmentResult.getAttemptId());
        entity.setUserId(assessmentResult.getUserId());
        entity.setAssessmentId(assessmentResult.getAssessmentId());

        entity.setResponseData(objectMapper.writeValueAsString(assessmentResult.getQuestions()));
        entity.setCreatedAt(LocalDateTime.now());
        assessmentResponseRepository.save(entity);
    }

    /**
     * Update the score in the database.
     *
     * @param userId       The user for whom the score is updated.
     * @param assessmentId The ID of the assessment.
     * @param score        The score to be updated.
     */
    public void updateScore(String userId, Long assessmentId, int score) {
        Score scoreEntity = new Score();
        scoreEntity.setUserId(userId);
        scoreEntity.setAssessmentId(assessmentId);
        scoreEntity.setScore(score);

        scoreRepository.save(scoreEntity);
    }

    public double calculateProgress(String username, String language) {
        // Step 1: Find all the assessment IDs for the given language
        List<Assessment> assessmentsForLanguage = assessmentRepository.findByLanguage(language);
        Set<Long> assessmentIdsForLanguage = assessmentsForLanguage.stream()
                .map(Assessment::getAssessmentId)
                .collect(Collectors.toSet());

        // Step 2: Filter the UserTestAttempt entries by the assessment IDs and the user ID
        List<UserTestAttempt> userAttemptsForLanguage = userTestAttemptRepository.findByUserIdAndAttemptedIsTrue(username);
        Set<Long> uniqueCompletedAssessmentIds = userAttemptsForLanguage.stream()
                .filter(attempt -> assessmentIdsForLanguage.contains(attempt.getAssessmentId()))
                .map(UserTestAttempt::getAssessmentId)
                .collect(Collectors.toSet());

        // Step 3: Count the number of unique assessment IDs found in the filtered UserTestAttempt entries
        int completedAssessmentCount = uniqueCompletedAssessmentIds.size();

        // Calculate the progress percentage
        double progressPercentage = (double) completedAssessmentCount / assessmentIdsForLanguage.size() * 100;

        return progressPercentage;
    }


}
