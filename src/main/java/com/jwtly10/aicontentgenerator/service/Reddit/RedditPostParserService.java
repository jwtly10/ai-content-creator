package com.jwtly10.aicontentgenerator.service.Reddit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwtly10.aicontentgenerator.exceptions.RedditPostParserException;
import com.jwtly10.aicontentgenerator.model.Reddit.RedditPost;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class RedditPostParserService {

    /**
     * Parses a Reddit post from a given URL
     *
     * @param postUrl URL of the Reddit post
     * @return RedditPost object
     * @throws RedditPostParserException if unable to parse the post
     */
    public RedditPost parseRedditPost(String postUrl) throws RedditPostParserException {
        log.info("Parsing reddit post: {}", postUrl);

        // Validate this is a valid url
        validatePostUrl(postUrl);

        try {
            String[] urlParts = postUrl.split("/");
            String postId = urlParts[urlParts.length - 2];
            String postSub = urlParts[4];

            // Make a request to the Reddit API
            RestTemplate restTemplate = new RestTemplate();
            String apiUrl = "https://www.reddit.com/r/" + postSub + "/comments/" + postId + ".json";
            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);

            // Parse JSON response
            String jsonResponse = response.getBody();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);

            String title = jsonNode.get(0).get("data").get("children").get(0).get("data").get("title").asText();
            String postDescription = jsonNode.get(0).get("data").get("children").get(0).get("data").get("selftext").asText()
                    .replaceAll("[\\n\\u00a0]", " ") // Remove newlines and non-breaking spaces
                    .replaceAll("\\s+", " ") // Remove extra spaces
                    .replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", ""); // Remove Markdown links
            String subreddit = jsonNode.get(0).get("data").get("children").get(0).get("data").get("subreddit").asText();

            log.info("Successfully parsed reddit post: {}", title);
            return RedditPost.builder()
                    .title(title)
                    .description(postDescription)
                    .subreddit("r/" + subreddit)
                    .build();
        } catch (Exception e) {
            log.error("Error parsing reddit post: {}", e.getMessage());
            throw new RedditPostParserException("Reddit Parsing failed:" + e.getMessage());
        }
    }

    /**
     * Validates a Reddit post URL
     *
     * @param postUrl URL of the Reddit post
     * @throws RedditPostParserException if the URL is invalid
     */
    public void validatePostUrl(String postUrl) throws RedditPostParserException {
        if (!postUrl.contains("reddit.com/r/") || !postUrl.contains("/comments/")) {
            throw new RedditPostParserException("Invalid Reddit post URL");
        }
    }

}
