package com.jwtly10.aicontentgenerator.service;

import com.jwtly10.aicontentgenerator.exceptions.UserServiceException;
import com.jwtly10.aicontentgenerator.model.UserVideo;
import com.jwtly10.aicontentgenerator.model.Video;
import com.jwtly10.aicontentgenerator.model.VideoProcessingState;
import com.jwtly10.aicontentgenerator.model.api.response.VideoGenResponse;
import com.jwtly10.aicontentgenerator.model.api.response.VideoListResponse;
import com.jwtly10.aicontentgenerator.repository.UserVideoDAOImpl;
import com.jwtly10.aicontentgenerator.repository.VideoDAOImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class VideoService {
    private final UserVideoDAOImpl userVideoDAOImpl;
    private final VideoDAOImpl videoDAOImpl;
    private final UserService userService;

    public VideoService(UserVideoDAOImpl userVideoDAOImpl, VideoDAOImpl videoDAOImpl, UserService userService) {
        this.userVideoDAOImpl = userVideoDAOImpl;
        this.videoDAOImpl = videoDAOImpl;
        this.userService = userService;
    }

    public void logNewVideoProcess(String processId) {
        int userId = userService.getLoggedInUserId();
        log.info("Logging new video process to DB");
        videoDAOImpl.create(Video.builder()
                .videoId(processId)
                .build());
        userVideoDAOImpl.create(
                UserVideo.builder()
                        .userId(userId)
                        .videoId(processId)
                        .state(VideoProcessingState.PENDING)
                        .build());
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
                .videos(videoDAOImpl.getAllVideoData(userId))
                .build();
    }

}
