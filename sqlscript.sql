CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE assessments (
    assessment_id SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    language VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE questions (
    question_id SERIAL PRIMARY KEY,
    assessment_id INT REFERENCES assessments(assessment_id),
    question_text TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE assessment_response (
    id SERIAL PRIMARY KEY,
    attempt_id BIGINT,
    user_id VARCHAR(255),
    assessment_id BIGINT,
    response_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE scores (
    score_id SERIAL PRIMARY KEY,
    user_id VARCHAR(50)
    assessment_id INT REFERENCES assessments(assessment_id),
    score INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE language_tests (
    test_id SERIAL PRIMARY KEY,
    language VARCHAR(50) NOT NULL,
    assessment_id INT REFERENCES assessments(assessment_id),
    UNIQUE(language, assessment_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_test_attempts (
    attempt_id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(user_id),
    assessment_id INT REFERENCES assessments(assessment_id),
    attempt_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, assessment_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
