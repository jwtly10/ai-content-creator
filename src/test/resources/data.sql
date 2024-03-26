DELETE
FROM user_video_tb;
DELETE
FROM video_content_tb;
DELETE
FROM video_tb;
DELETE
FROM users_tb;

INSERT INTO users_tb (id, firstname, lastname, password, email, role, created_at, updated_at)
VALUES (6, 'Test', 'User', '$2a$10$OmFVS2EHNBHB0w.J.hJM0uHuCAe.GgqBoRhHDObQ9H7s4yrjWx/Va', 'test_user@gmail.com',
        'USER', '2024-01-01 17:54:53', '2024-01-01 17:54:53');

INSERT INTO video_tb (video_id, file_url, file_name, length, upload_date, created_at, updated_at)
VALUES ('0b9159cd-5c7c-46a0-9a59-c58f0de17b65',
        'https://mwkisxjoqbhcysakueek.supabase.co/storage/v1/object/public/ai-content-generator/test-media/0b9159cd-5c7c-46a0-9a59-c58f0de17b65_final.mp4',
        '0b9159cd-5c7c-46a0-9a59-c58f0de17b65_final.mp4', 99, '2024-01-07 11:11:19', '2024-01-07 11:10:00',
        '2024-01-07 11:11:18');

INSERT INTO video_content_tb(video_id, title, subreddit, content, backgroundVideo)
VALUES ('0b9159cd-5c7c-46a0-9a59-c58f0de17b65',
        'AITA: For not continuing my reception after my husband went behind my back',
        'r/TestSubReddit',
        'Test content for a successful video',
        'minecraft-1.mp4');


INSERT INTO user_video_tb (user_id, video_id, state, error_msg, created_at, updated_at)
VALUES (6, '0b9159cd-5c7c-46a0-9a59-c58f0de17b65', 'COMPLETED', null, '2024-01-07 11:10:00', '2024-01-07 11:11:18');

INSERT INTO video_tb (video_id, file_url, file_name, length, upload_date, created_at, updated_at)
VALUES ('2343ebe6-e9b4-4853-9837-7bc1808900e2',
        'https://mwkisxjoqbhcysakueek.supabase.co/storage/v1/object/public/ai-content-generator/test-media/2343ebe6-e9b4-4853-9837-7bc1808900e2_final.mp4',
        '2343ebe6-e9b4-4853-9837-7bc1808900e2_final.mp4', 99, '2024-01-07 11:09:03', '2024-01-07 11:07:47',
        '2024-01-07 11:09:03');

INSERT INTO video_content_tb(video_id, title, subreddit, content, backgroundVideo)
VALUES ('2343ebe6-e9b4-4853-9837-7bc1808900e2',
        'AITA: For not continuing my reception after my husband went behind my back',
        'r/TestSubReddit',
        'Test content for a successful video',
        'minecraft-1.mp4');

INSERT INTO user_video_tb (user_id, video_id, state, error_msg, created_at, updated_at)
VALUES (6, '2343ebe6-e9b4-4853-9837-7bc1808900e2', 'COMPLETED', null, '2024-01-07 11:07:47', '2024-01-07 11:09:03');


INSERT INTO video_tb (video_id, file_url, file_name, length, upload_date, created_at, updated_at)
VALUES ('9d94fcb2-3e16-4201-bff8-4f46b8c34291',
        '',
        '', null, null, '2024-01-07 11:07:47',
        '2024-01-07 11:09:03');

INSERT INTO video_content_tb(video_id, title, subreddit, content, backgroundVideo)
VALUES ('9d94fcb2-3e16-4201-bff8-4f46b8c34291',
        'AITA: For not continuing my reception after my husband went behind my back',
        'r/TestSubReddit',
        'Test content for a failed video',
        'minecraft-1.mp4');

INSERT INTO user_video_tb (user_id, video_id, state, error_msg, created_at, updated_at)
VALUES (6, '9d94fcb2-3e16-4201-bff8-4f46b8c34291', 'FAILED',
        'Error while aligning text with audio: No route to host', '2024-01-07 11:06:49', '2024-01-07 11:06:57');
