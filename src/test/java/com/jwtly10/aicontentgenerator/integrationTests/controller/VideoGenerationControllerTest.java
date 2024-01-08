package com.jwtly10.aicontentgenerator.integrationTests.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.jwtly10.aicontentgenerator.baseTests.ControllerTestBase;
import com.jwtly10.aicontentgenerator.model.Reddit.RedditPost;
import com.jwtly10.aicontentgenerator.model.UserVideo;
import com.jwtly10.aicontentgenerator.model.Video;
import com.jwtly10.aicontentgenerator.model.VideoContent;
import com.jwtly10.aicontentgenerator.model.VideoProcessingState;
import com.jwtly10.aicontentgenerator.repository.UserVideoDAO;
import com.jwtly10.aicontentgenerator.repository.VideoContentDAO;
import com.jwtly10.aicontentgenerator.repository.VideoDAO;
import com.jwtly10.aicontentgenerator.service.Reddit.RedditPostParserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
public class VideoGenerationControllerTest extends ControllerTestBase {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RedditPostParserService redditPostParserService;

    @Autowired
    private VideoContentDAO<VideoContent> videoContentDAO;

    @Autowired
    private UserVideoDAO<UserVideo> userVideoDAO;

    @Autowired
    private VideoDAO<Video> videoDao;

    @Test
    public void generateVideoFromRedditUrl() throws Exception {
        RedditPost redditPost = RedditPost.builder()
                .title("Mocked Title")
                .subreddit("Mocked Subreddit")
                .content("Mocked Content")
                .build();

        when(redditPostParserService.parseRedditPost("https://www.reddit.com/r/AmItheAsshole/comments/191cu3z/aita_for_not_agreeing_with_my_wifes_seemingly/"))
                .thenReturn(redditPost);

        String jwtToken = getLoginToken();

        JsonObject req = new JsonObject();
        req.addProperty("url", "https://www.reddit.com/r/AmItheAsshole/comments/191cu3z/aita_for_not_agreeing_with_my_wifes_seemingly/");
        req.addProperty("backgroundVideo", "minecraft_parkour_1");


        ResultActions resultActions = mockMvc.perform(post("/api/v1/video/generate/reddit")
                        .header("Authorization", "Bearer " + jwtToken)
                        .header("Content-Type", "application/json")
                        .content(req.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processId").exists());

        String generatedId = resultActions.andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(generatedId);
        String processId = jsonNode.get("processId").asText();

        // Check if video content was created
        VideoContent videoContent = videoContentDAO.get(processId).orElseThrow(
                () -> new Exception("Video content not found")
        );

        assertEquals(videoContent.getTitle(), redditPost.getTitle());
        assertEquals(videoContent.getSubreddit(), redditPost.getSubreddit());
        assertEquals(videoContent.getContent(), redditPost.getContent());

        // Check if video record created
        Video video = videoDao.get(processId).orElseThrow(
                () -> new Exception("Video not found")
        );

        assertEquals(video.getVideoId(), processId);
        assertNull(video.getFileName());
        assertNull(video.getFileUrl());


        // Check if user video record was created
        UserVideo userVideo = userVideoDAO.get(processId, 6).orElseThrow(
                () -> new Exception("User video not found")
        );

        assertEquals(userVideo.getVideoId(), processId);
        assertEquals(userVideo.getUserId(), 6);
        assertEquals(userVideo.getState(), VideoProcessingState.PENDING);
    }
}
