package com.jwtly10.aicontentgenerator.unitTests.utils;

import com.jwtly10.aicontentgenerator.baseTests.TestBase;
import com.jwtly10.aicontentgenerator.exceptions.SRTGenerationException;
import com.jwtly10.aicontentgenerator.utils.FileUtils;
import com.jwtly10.aicontentgenerator.utils.GentleAlignerUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


/**
 * GentleAlignerTest
 */
@Slf4j
@SpringBootTest
public class GentleAlignerTest extends TestBase {
    @Autowired
    private GentleAlignerUtil gentleAlignerUtil;

    @Test
    public void testGenerateSRT() {
        String fileUUID = FileUtils.generateUUID();
        String test_audio_loc =
                getTestFileLocally("example_audio.mp3").orElseThrow();

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
                getTestFileLocally("test_large_audio.mp3").orElseThrow();

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

    @Test
    public void testRealContent() throws IOException {
        String test_audio_path = new ClassPathResource("/local_media/test_audio.mp3").getFile().getAbsolutePath();

        String expected_srt_path = new ClassPathResource("/local_media/test_srt.srt").getFile().getAbsolutePath();

        String content = "My now husband Lucas (26) and I (F,25) were getting married. We decided to tie the knot as we were having a little girl together and are madly in love. So leading up to the wedding day Lucas told me that his best man (Jacob) wanted to propose to his girlfriend as it would be a great time and it is a nice venue to do it at. I said that I didn’t want him to propose at our wedding as it is our special moment, not theirs and that they can do it sometime else. Lucas told me that his friend was mad that I didn’t agree. I just wanted the wedding to be about us because it was our special day. After that disagreement I thought nothing of it. Fast forward to my wedding day. We had finished the church service and now we’re at the reception were all having fun eating. I’m eating my food and then Jacob stops the music at the DJ booth to make an announcement. I just knew from that moment he was going to propose. I look to see where Lucas was and he was holding red and white roses walking out to stand in front of Chloe(Jacob’s girlfriend)spelling out. “Will you marry me?”I was shocked that they went behind my back when I said no. I got up out of seat and walked out. It’s been 2 days since the wedding and my husband cursed me out for not letting them have a special moment. I responded with “I wanted the day to be about us because it’s our wedding not theirs and I am happy for them but the worst thing was even though I said no you went behind my back about it.”Since that argument he moved to the guest bedroom and now most of my friends are cursing me out on all my socials. AITA?";


        String fileUUID = FileUtils.generateUUID();
        String outSRT = gentleAlignerUtil.alignAndGenerateSRT(
                test_audio_path, content, fileUUID);

        assertFalse(outSRT.isEmpty(), "Output SRT file path is empty");
        assertTrue(compareTextFiles(expected_srt_path, outSRT));
    }
}