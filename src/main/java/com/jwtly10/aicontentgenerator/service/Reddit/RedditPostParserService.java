package com.jwtly10.aicontentgenerator.service.Reddit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwtly10.aicontentgenerator.exceptions.GenerationRuleException;
import com.jwtly10.aicontentgenerator.exceptions.RedditPostParserException;
import com.jwtly10.aicontentgenerator.model.Reddit.RedditPost;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class RedditPostParserService {

    @Value("${smartproxy.apiKey}")
    private String apiKey;

    @Value("${use.proxy:false}")
    private boolean useProxy;

    /**
     * Parses a Reddit post from a given URL
     *
     * @param postUrl URL of the Reddit post
     * @return RedditPost object
     * @throws RedditPostParserException if unable to parse the post
     */
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 10000))
    public RedditPost parseRedditPost(String postUrl) throws RedditPostParserException, GenerationRuleException {
        try {
            // Validate this is a valid url
            validatePostUrl(postUrl);

            if (useProxy) {
                return parseRedditPostWithProxy(postUrl);
            } else {
                return parseRedditPostLocally(postUrl);
            }

        } catch (GenerationRuleException e) {
            // We are OK with this exception, just throw it up, no need to retry
            log.error("Accepted Error parsing reddit post.", e);
            throw e;
        } catch (Exception e) {
            log.error("Error parsing reddit post.", e);
            throw new RedditPostParserException("Reddit Parsing failed: " + e.getMessage());
        }
    }

    public RedditPost parseRedditPostWithProxy(String postUrl) {
        try {
            log.info("Parsing reddit post with proxy: {}", postUrl);

            String apiUrl = "https://scraper-api.smartproxy.com/v2/scrape";
            String target = "reddit_post";
            String locale = "en-us";
            String geo = "United States";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            headers.set("Authorization", "Basic " + apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = String.format(
                    "{ \"target\": \"%s\", \"url\": \"%s\", \"locale\": \"%s\", \"geo\": \"%s\" }",
                    target, postUrl, locale, geo);

            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

            String responseBody = response.getBody();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            JsonNode post = jsonNode.get("results").get(0).get("content");

            String title = post.get(0).get("data").get("children").get(0).get("data").get("title").asText();
            String postDescription = post.get(0).get("data").get("children").get(0).get("data").get("selftext").asText()
                    .replaceAll("[\\n\\u00a0]", " ") // Remove newlines and non-breaking spaces
                    .replaceAll("\\s+", " ") // Remove extra spaces
                    .replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", ""); // Remove Markdown links
            String subreddit = post.get(0).get("data").get("children").get(0).get("data").get("subreddit").asText();

            if (postDescription.length() < 10) {
                throw new GenerationRuleException("Reddit post description is too short. Or this post is not valid for generation.");
            }

            if (postDescription.length() > 3000) { // On average of 5 chars per word in English, this is 600 words
                throw new GenerationRuleException("Reddit post description is too long.");
            }

            log.info("Successfully parsed reddit post: {}", title);
            return RedditPost.builder()
                    .title(title)
                    .content(postDescription)
                    .subreddit("r/" + subreddit)
                    .build();
        } catch (GenerationRuleException e) {
            throw e;
        } catch (Exception e) {
            throw new RedditPostParserException(e.getMessage());
        }
    }


    public RedditPost parseRedditPostLocally(String postUrl) throws RedditPostParserException, GenerationRuleException {
        try {
            log.info("Parsing reddit post locally: {}", postUrl);

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

            if (postDescription.length() < 10) {
                throw new GenerationRuleException("Reddit post description is too short. Or this post is not valid for generation.");
            }

            if (postDescription.length() > 3000) { // On average of 5 chars per word in English, this is 600 words
                throw new GenerationRuleException("Reddit post description is too long.");
            }

            log.info("Successfully parsed reddit post: {}", title);
            return RedditPost.builder()
                    .title(title)
                    .content(postDescription)
                    .subreddit("r/" + subreddit)
                    .build();
        } catch (GenerationRuleException e) {
            throw e;
        } catch (Exception e) {
            throw new RedditPostParserException(e.getMessage());
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
