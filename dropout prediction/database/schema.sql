-- Student Dropout Prediction System - Database Schema
CREATE DATABASE IF NOT EXISTS dropout_db;
USE dropout_db;

CREATE TABLE IF NOT EXISTS users (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(20)  NOT NULL DEFAULT 'USER'
);

CREATE TABLE IF NOT EXISTS students (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                  VARCHAR(100)   NOT NULL,
    attendance_percentage DOUBLE         NOT NULL,
    gpa                   DOUBLE         NOT NULL,
    behavior_score        INT            NOT NULL,
    engagement_level      INT            NOT NULL,
    family_income         DOUBLE         NOT NULL,
    scholarship           TINYINT(1)     NOT NULL DEFAULT 0,
    fee_status            VARCHAR(20)    NOT NULL DEFAULT 'PAID',
    counseling            TINYINT(1)     NOT NULL DEFAULT 0,
    stress_level          INT            NOT NULL,
    dropout_risk          VARCHAR(10)    DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS student_notes (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT       NOT NULL,
    note       VARCHAR(1000) NOT NULL,
    added_by   VARCHAR(50)  NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS student_history (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id            BIGINT      NOT NULL,
    attendance_percentage DOUBLE,
    gpa                   DOUBLE,
    behavior_score        INT,
    engagement_level      INT,
    stress_level          INT,
    dropout_risk          VARCHAR(10),
    recorded_at           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
