# Script to popuplate DB with test data

## integration/VideoServiceTest.java
INSERT INTO video_tb (video_id, title, file_url, file_name)
VALUES ("7db783d8-a68a-4e3e-b390-81d7e7cf3941", "test_title", "test_url", "test_name");
INSERT INTO user_video_tb (user_id, video_id, state)
VALUES (1, "7db783d8-a68a-4e3e-b390-81d7e7cf3941", "COMPLETED");

INSERT INTO video_tb (video_id, title, file_url, file_name)
VALUES ("b6d03230-96a2-455d-9c8d-afdsgfd07363", "test_title", "test_url", "test_name");
INSERT INTO user_video_tb (user_id, video_id, state)
VALUES (6, "b6d03230-96a2-455d-9c8d-afdsgfd07363", "PROCESSING");
