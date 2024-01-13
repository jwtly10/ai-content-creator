package com.jwtly10.aicontentgenerator.integrationTests.jobs;

import com.jwtly10.aicontentgenerator.baseTests.IntegrationTestBase;
import com.jwtly10.aicontentgenerator.jobs.VideoGenerationJob;
import com.jwtly10.aicontentgenerator.model.Reddit.RedditPost;
import com.jwtly10.aicontentgenerator.model.UserVideo;
import com.jwtly10.aicontentgenerator.model.VideoProcessingState;
import com.jwtly10.aicontentgenerator.repository.UserVideoDAO;
import com.jwtly10.aicontentgenerator.service.StorageService;
import com.jwtly10.aicontentgenerator.service.VideoService;
import com.jwtly10.aicontentgenerator.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * VideoGenerationJobTest
 */
@SpringBootTest
@Slf4j
@Async
public class VideoGenerationJobTest extends IntegrationTestBase {

    @Autowired
    private VideoGenerationJob videoGenerationJob;

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserVideoDAO<UserVideo> userVideoDAO;

    @Autowired
    private StorageService storageService;

    //    @Test
    public void testVideoGenerationJob() throws ExecutionException, InterruptedException {

        // Login as test user
        setupAuthentication();

        // Create 3 RedditPosts
        RedditPost post1 = RedditPost.builder()
                .title("AITA if my title is too long and I don't care? I don't think I am but I want to know what you think? " +
                        "I don't think I am but I want to know what you think? I don't think I am but I want to know what you think?")
                .subreddit("Test Subreddit 1")
                .content("Test Content 1")
                .build();

        RedditPost post2 = RedditPost.builder()
                .title("Test Title 2")
                .subreddit("Test Subreddit 2")
                .content("Test Content 2")
                .build();

        RedditPost post3 = RedditPost.builder()
                .title("Test Title 3")
                .subreddit("Test Subreddit 3")
                .content("Test Content 3")
                .build();

        List<RedditPost> posts = Arrays.asList(post1, post2, post3);

        List<String> processIds = new ArrayList<>();

        // Queue video generation for each post
        posts.forEach(post -> {
            String processId = FileUtils.generateUUID();
            videoService.queueVideoGeneration(post, processId, "Minecraft Parkour");
            processIds.add(processId);
        });

        // Wait for video generation to complete
        CompletableFuture<Void> asyncOperation = CompletableFuture.runAsync(() -> {
            // Trigger asynchronous task
            videoGenerationJob.run();
        });

        // Wait for the asynchronous task to complete
        asyncOperation.get();

        // Check if video generation is complete
        IntStream.range(0, processIds.size())
                .forEach(index -> {
                    String processId = processIds.get(index);
                    UserVideo userVideo = userVideoDAO
                            .get(processId, 6)
                            .orElseThrow(() -> new RuntimeException("User video not found"));

                    if (index == 0) {
                        // The first post should fail
                        assertEquals(userVideo.getState(), VideoProcessingState.FAILED);
                        assertEquals("Chosen title text is too long for image", userVideo.getError());
                        log.info("Different assertion for the first video (processId: {}, state is: {})", processId, userVideo.getState());
                    } else {
                        assertEquals(userVideo.getState(), VideoProcessingState.COMPLETED);
                        log.info("Video generation completed for processId: {}, state is: {}", processId, userVideo.getState());
                    }
                });

        // Delete videos from S3
        // The first video failed so we dont need to delete it
//        storageService.deleteVideo(processIds.get(1) + "_final.mp4");
//        storageService.deleteVideo(processIds.get(2) + "_final.mp4");
    }
}
