CREATE TABLE assessments (
    assessment_id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    language VARCHAR(50) NOT NULL,
    difficulty_level VARCHAR(50) NOT NULL
);

CREATE TABLE assessment_response (
    id SERIAL PRIMARY KEY,
    attempt_id BIGINT,
    user_id VARCHAR(50),
    assessment_id BIGINT,
    response_data JSONB,
    created_at TIMESTAMP
);





CREATE TABLE scores (
    score_id SERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    assessment_id BIGINT NOT NULL,
    score INTEGER,
    FOREIGN KEY (assessment_id) REFERENCES assessments(assessment_id)
);

CREATE TABLE user_test_attempts (
    attempt_id SERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    assessment_id BIGINT NOT NULL,
    attempt_date TIMESTAMP,
    attempted BOOLEAN NOT NULL,
    FOREIGN KEY (assessment_id) REFERENCES assessments(assessment_id)
);


INSERT INTO assessments (title, description, language, difficulty_level)
VALUES
    ('vocab', 'Test your English language proficiency.', 'en', 'high'),
    ('vocab', 'Test your French vocabulary knowledge.', 'fr', 'high'),
    ('vocab', 'Test your Spanish grammar skills.', 'es', 'high'),
    ('vocab', 'Test your English language proficiency.', 'en', 'medium'),
    ('vocab', 'Test your French vocabulary knowledge.', 'fr', 'medium'),
    ('vocab', 'Test your Spanish grammar skills.', 'es', 'medium'),
    ('vocab', 'Test your English language proficiency.', 'en', 'low'),
    ('vocab', 'Test your French vocabulary knowledge.', 'fr', 'low'),
    ('vocab', 'Test your Spanish grammar skills.', 'es', 'low');
