CREATE SCHEMA IF NOT EXISTS local;

USE local;

CREATE TABLE IF NOT EXISTS local.users_tb
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    firstname  VARCHAR(255)        NOT NULL,
    lastname   VARCHAR(255)        NOT NULL,
    password   TEXT                NOT NULL,
    email      VARCHAR(255) UNIQUE NOT NULL,
    role       VARCHAR(50)         NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS local.video_tb
(
    video_id    VARCHAR(255) PRIMARY KEY,
    file_url    VARCHAR(255),
    file_name   VARCHAR(255),
    length      INT,
    upload_date TIMESTAMP,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS local.video_content_tb
(
    video_id        VARCHAR(255) PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    subreddit       VARCHAR(255) NOT NULL,
    content         TEXT         NOT NULL,
    backgroundVideo VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_video_content_id FOREIGN KEY (video_id) REFERENCES video_tb (video_id)
);

CREATE TABLE IF NOT EXISTS local.user_video_tb
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT,
    video_id   VARCHAR(255) NOT NULL,
    state      VARCHAR(50)  NOT NULL,
    error_msg  TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users_tb (id),
    CONSTRAINT fk_video_id FOREIGN KEY (video_id) REFERENCES video_tb (video_id)
);

CREATE TABLE IF NOT EXISTS local.newsletter_tb
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);