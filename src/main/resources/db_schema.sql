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

