package com.jwtly10.aicontentgenerator.service;

import com.jwtly10.aicontentgenerator.exceptions.DatabaseException;
import com.jwtly10.aicontentgenerator.exceptions.UserServiceException;
import com.jwtly10.aicontentgenerator.model.Reddit.RedditPost;
import com.jwtly10.aicontentgenerator.model.UserVideo;
import com.jwtly10.aicontentgenerator.model.Video;
import com.jwtly10.aicontentgenerator.model.VideoContent;
import com.jwtly10.aicontentgenerator.model.VideoProcessingState;
import com.jwtly10.aicontentgenerator.model.api.response.VideoGenResponse;
import com.jwtly10.aicontentgenerator.model.api.response.VideoListResponse;
import com.jwtly10.aicontentgenerator.repository.UserVideoDAO;
import com.jwtly10.aicontentgenerator.repository.VideoContentDAO;
import com.jwtly10.aicontentgenerator.repository.VideoDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class VideoService {
    private final UserVideoDAO<UserVideo> userVideoDAOImpl;
    private final VideoDAO<Video> videoDAOImpl;
    private final VideoContentDAO<VideoContent> videoContentDAOImpl;
    private final UserService userService;


    public VideoService(UserVideoDAO<UserVideo> userVideoDAOImpl, VideoDAO<Video> videoDAOImpl, VideoContentDAO<VideoContent> videoContentDAOImpl, UserService userService) {
        this.userVideoDAOImpl = userVideoDAOImpl;
        this.videoDAOImpl = videoDAOImpl;
        this.videoContentDAOImpl = videoContentDAOImpl;
        this.userService = userService;
    }

    /**
     * Queue video generation
     *
     * @param post      Reddit post
     * @param processId Process ID
     */
    @Transactional
    public void queueVideoGeneration(RedditPost post, String processId, String backgroundVideo) {
        log.info("Queueing video generation");
        int userId = userService.getLoggedInUserId();

        try {
            // Create video_tb record
            videoDAOImpl.create(Video.builder()
                    .videoId(processId)
                    .build());

            // Create user_video_tb record
            userVideoDAOImpl.create(
                    UserVideo.builder()
                            .userId(userId)
                            .videoId(processId)
                            .state(VideoProcessingState.PENDING)
                            .build());

            // Create video_content_tb record
            videoContentDAOImpl.create(
                    VideoContent.builder()
                            .videoId(processId)
                            .title(post.getTitle())
                            .subreddit(post.getSubreddit())
                            .content(post.getContent())
                            .backgroundVideo(backgroundVideo)
                            .build()
            );
        } catch (DatabaseException e) {
            log.error("Error queueing video generation: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public void updateVideoProcessLog(String processId, VideoProcessingState state, String error) {
        if (state.equals(VideoProcessingState.COMPLETED)) {
            log.info("Logging processing completed");
            // Set upload date here too
            userVideoDAOImpl.update(
                    UserVideo.builder()
                            .state(state)
                            .error(error)
                            .build(), processId);
            return;
        }

        log.info("Logging video process update");
        userVideoDAOImpl.update(
                UserVideo.builder()
                        .state(state)
                        .error(error)
                        .build(), processId);
    }

    /**
     * Update video record
     *
     * @param video Video
     */
    public void updateVideo(Video video) {
        log.info("Logging video update");
        videoDAOImpl.update(video);
    }

    /**
     * Check video processing status
     *
     * @param processId Process ID
     * @return VideoGenResponse
     * @throws UserServiceException if process ID not found for authenticated user
     */
    public VideoGenResponse checkStatus(String processId) throws UserServiceException {
        int userId = userService.getLoggedInUserId();
        Optional<UserVideo> userVideo = userVideoDAOImpl.get(processId, userId);
        if (userVideo.isEmpty()) {
            throw new UserServiceException("Process ID not found for authenticated user");
        }

        return VideoGenResponse.builder()
                .processId(userVideo.get().getVideoId())
                .status(userVideo.get().getState())
                .error(userVideo.get().getError())
                .build();
    }

    /**
     * Get video for authenticated user
     *
     * @return Video
     * @throws UserServiceException if process ID not found for authenticated user
     */
    public VideoListResponse getVideos() {
        int userId = userService.getLoggedInUserId();
        return VideoListResponse.builder()
                .build();
    }

    public Optional<Video> getVideo(String processId) {
        return videoDAOImpl.get(processId);
    }

    /**
     * Delete user video
     *
     * @param processId Process ID
     * @return VideoGenResponse
     * @throws UserServiceException if process ID not found for authenticated user
     */
    public VideoGenResponse deleteVideo(String processId) throws UserServiceException {
        log.info("Deleting video for process ID: {}", processId);
        int userId = userService.getLoggedInUserId();
        Optional<UserVideo> userVideo = userVideoDAOImpl.get(processId, userId);
        if (userVideo.isEmpty()) {
            throw new UserServiceException("Process ID not found for authenticated user");
        }

        userVideoDAOImpl.update(
                UserVideo.builder()
                        .state(VideoProcessingState.DELETED)
                        .build(), processId);

        return VideoGenResponse.builder()
                .processId(userVideo.get().getVideoId())
                .status(VideoProcessingState.DELETED)
                .error(userVideo.get().getError())
                .build();
    }
}
