CREATE SCHEMA IF NOT EXISTS dev;

CREATE TABLE IF NOT EXISTS dev.users_tb
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

CREATE TABLE IF NOT EXISTS dev.video_tb
(
    video_id    VARCHAR(255) PRIMARY KEY,
    title       VARCHAR(255),
    file_url    VARCHAR(255),
    file_name   VARCHAR(255),
    length      INT,
    upload_date TIMESTAMP,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dev.user_video_tb
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT,
    video_id   VARCHAR(255) NOT NULL,
    state      VARCHAR(50)  NOT NULL,
    error_msg  TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES dev.users_tb (id),
    CONSTRAINT fk_video_id FOREIGN KEY (video_id) REFERENCES dev.video_tb (video_id)
);
