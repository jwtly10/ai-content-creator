CREATE SCHEMA dev;

CREATE TABLE users_tb
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

CREATE TABLE user_video_tb
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT,
    file_uuid   VARCHAR(255) NOT NULL,
    file_name   VARCHAR(255),
    state       VARCHAR(50)  NOT NULL,
    error_msg   TEXT,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users_tb (id)
);