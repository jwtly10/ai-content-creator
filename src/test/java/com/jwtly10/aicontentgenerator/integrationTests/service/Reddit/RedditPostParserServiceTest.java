package com.jwtly10.aicontentgenerator.integrationTests.service.Reddit;

import com.jwtly10.aicontentgenerator.IntegrationTestBase;
import com.jwtly10.aicontentgenerator.model.Reddit.RedditPost;
import com.jwtly10.aicontentgenerator.service.Reddit.RedditPostParserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RedditPostParserServiceTest extends IntegrationTestBase {

    @Autowired
    private RedditPostParserService redditPostParserService;

    @Test
    void parseRedditPost() {

        RedditPost expectedPost = RedditPost.builder()
                .title("AITA for making my baby laugh at a restaurant")
                .subreddit("r/AmItheAsshole")
                .description("It was my wife's birthday yesterday. She picked out a mid tier restaurant to go to for her birthday. This was no chili's level but not high end either. We went at 5:30 on a Wednesday, so not that busy. We have a 10 month old who's just about the happiest kid ever. Nearly anything I do makes him laugh. Well at dinner I was making him laugh. He'd throw in some happy yelling. Maybe got a touch loud but he was in a great mood. Well the table next to us had an issue with what I was doing and asked me to stop. They told us to keep it down. I'm like he's laughing thats all. Him laughing is an issue? They just repeat that he is too loud, if he is going to be like this they suggested we stay home. I tell them to leave us alone and continue making my son laugh. I overheard them reference me as an asshole. They requested to move tables and did. But was I the asshole for making my baby laugh? Edit: This was a 3-5 minute interaction with my son while waiting for the check. It was 90% giggling. The other Hour plus we were there it was just him being quiet or eating or going bahbahbah over and over. There was no extended shrieking at all that occurred. This is a good example of what he did. ")
                .build();

        String url = "https://www.reddit.com/r/AmItheAsshole/comments/15h4tpt/aita_for_making_my_baby_laugh_at_a_restaurant/";

        RedditPost actualPost = redditPostParserService.parseRedditPost(url);

        assertEquals(expectedPost, actualPost);
    }
}