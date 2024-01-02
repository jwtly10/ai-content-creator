package com.jwtly10.aicontentgenerator.unitTests.utils;

import com.jwtly10.aicontentgenerator.BaseFileTest;
import com.jwtly10.aicontentgenerator.exceptions.SRTGenerationException;
import com.jwtly10.aicontentgenerator.utils.FileUtils;
import com.jwtly10.aicontentgenerator.utils.GentleAlignerUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


/**
 * GentleAlignerTest
 */
@Slf4j
@SpringBootTest
public class GentleAlignerTest extends BaseFileTest {
    @Autowired
    private GentleAlignerUtil gentleAlignerUtil;

    @Test
    public void testGenerateSRT() {
        String fileUUID = FileUtils.generateUUID();
        String test_audio_loc =
                getFileLocally("example_audio.mp3").orElseThrow();

        try {

            String content = """
                    Once upon a time, in the heart of the Enchanted Forest, there existed a quaint village named Eldoria. 
                    This mystical realm was known for its vibrant colors and magical creatures that roamed freely among the 
                    ancient trees. At the edge of the village stood an old, wise oak tree named Eldor, which was said to hold 
                    the secrets of the forest.
                    One day, a curious young girl named Luna discovered a hidden path leading to the base of Eldor. 
                    Intrigued by the whispers of the wind and the soft glow surrounding the tree, Luna felt a connection 
                    with the ancient oak. To her amazement, Eldor spoke to her in a gentle, melodic voice that resonated 
                    through the air
                    """;

            String outSRT = gentleAlignerUtil.alignAndGenerateSRT(
                    test_audio_loc, content, fileUUID);

            assertFalse(outSRT.isEmpty(), "Output SRT file path is empty");
            assertEquals(outSRT, ffmpegTmpPath + fileUUID + ".srt");
        } catch (SRTGenerationException e) {
            fail();
            log.error("Failed to generate SRT file");
        }

        cleanTempFiles(fileUUID);
        cleanUpFiles(test_audio_loc);
    }

    @Test
    public void testGenerateLargeSRT1() {
        String fileUUID = FileUtils.generateUUID();
        String test_audio_loc =
                getFileLocally("test_large_audio.mp3").orElseThrow();

        try {

            String content = """
                    I (27F) am in a bit of a dilemma and could really use some advice.
                    I've been seeing this guy for a couple months, and we decided to go to a nice high-end restaurant for a date. 
                    Initially I was going to wear a nice dark blue dress that I like to wear out, but he asked me to wear a 
                    different white dress that I had shown him once as it matched his outfit (I’ve never had a guy ask me this).
                    The white dress in question was a gift from my late grandmother and was quite expensive, so I was reluctant 
                    but agreed and just asked that we didn’t go anywhere after where I might spill something on it or otherwise 
                    mess it up (he mentioned clubbing after dinner which is why I said that, I didn’t want to risk messing the dress up) 
                    and he said we could just go to dinner and I could change out of it before doing anything else. Great!
                    However, the evening took a turn for the worse when he accidentally spilled his red wine all over my dress.
                    He had gotten an unexpected call and when he tried to quickly mute his ringer, his elbow hit his glass and 
                    it spilled all in the lap area of my dress before I could react. It was completely drenched and stained. 
                    He was apologetic at the time, and I tried to be cool about it, but inside, I was devastated, especially 
                    since I had mentioned specifically how I wanted to be careful wearing it.
                    Later, I mentioned to him that the dress was very expensive and asked if he'd be willing to help with the 
                    cost of cleaning or replacing it. To get it professionally cleaned and the stain removed would cost $100, 
                    which I asked him to pay half of. To my surprise, he got quite defensive. He argued that it was an accident 
                    and that I was being unreasonable for expecting him to pay for something like that, and that it was my fault 
                    for wearing it out knowing that it could’ve happened.
                    I feel like it's a matter of principle. Yes, it was an accident, but the dress is ruined, and it was extremely 
                    sentimental to me not to mention a valuable dress. He thinks I'm being materialistic and making a big deal out of nothing. 
                    Now, I'm not sure how to feel about his reaction. AITA for expecting him to cover the cost?
                    """;

            String outSRT = gentleAlignerUtil.alignAndGenerateSRT(
                    test_audio_loc, content, fileUUID);

            assertFalse(outSRT.isEmpty(), "Output SRT file path is empty");
            assertEquals(outSRT, ffmpegTmpPath + fileUUID + ".srt");
        } catch (SRTGenerationException e) {
            fail();
            log.error("Failed to generate SRT file");
        }

        cleanTempFiles(fileUUID);
        cleanUpFiles(test_audio_loc);
    }


}
