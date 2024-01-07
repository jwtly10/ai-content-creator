package com.jwtly10.aicontentgenerator.service.Reddit;

import com.jwtly10.aicontentgenerator.exceptions.ImageGenerationException;
import com.jwtly10.aicontentgenerator.model.Reddit.RedditTitle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RedditTitleImageGenerator {

    @Value("${file.tmp.path}")
    private String tmpPath;

    /**
     * Generates an image for the given reddit title.
     *
     * @param redditTitle The reddit title to generate an image for.
     * @param fileId      The id of the file to generate.
     * @return The path to the generated image.
     * @throws ImageGenerationException If an error occurs while generating the image.
     */
    public String generateImage(RedditTitle redditTitle, String fileId) throws ImageGenerationException {
        log.info("Generating image for reddit title: {}", redditTitle.getTitle());
        String template = "";
        try {
            template =
                    new ClassPathResource("templates/reddit_title_template.png").getFile().getAbsolutePath();
        } catch (IOException e) {
            log.error("Error reading template file: {}", e.getMessage());
            throw new ImageGenerationException("Error reading template file: " + e.getMessage());
        }

        Font font = new Font("Arial", Font.BOLD, 28);
        Color textColor = Color.BLACK;

        String outputPath = tmpPath + fileId + ".png";

        return addTextToImage(template, outputPath, redditTitle.getTitle(), font, textColor);
    }

    /**
     * Adds the given text to the given image.
     *
     * @param inputImagePath  The path to the input image.
     * @param outputImagePath The path to the output image.
     * @param text            The text to add to the image.
     * @param baseFont        The base font to start the search from.
     * @param color           The color of the text.
     * @return The path to the output image.
     * @throws ImageGenerationException If an error occurs while adding the text to the image.
     */
    private String addTextToImage(String inputImagePath, String outputImagePath, String text, Font baseFont, Color color) throws ImageGenerationException {
        try {
            BufferedImage originalImage = ImageIO.read(new File(inputImagePath));

            BufferedImage newImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = newImage.createGraphics();

            // Improve image quality
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

            graphics.drawImage(originalImage, 0, 0, null);

            int maxWidth = 760;

            int x = 400;
            int y = 780;
            // This bug with title positioning may be a resolution issue
//            int y = 820;

            Font font = baseFont.deriveFont((float) 34);

            String[] lines = breakTextIntoLines(text, font, maxWidth);

            if (lines.length > 3) {
                log.error("Error adding text to image: Text too long");
                throw new ImageGenerationException("Chosen title text is too long for image");
            }

            if (lines.length > 1) {
                Font multiLineFont = baseFont.deriveFont((float) font.getSize());
                lines = breakTextIntoLines(text, multiLineFont, maxWidth);
            }

            graphics.setFont(font);
            graphics.setColor(color);

            for (int i = 0; i < lines.length; i++) {
                graphics.drawString(lines[i], x, y + i * graphics.getFontMetrics().getHeight());
            }

            ImageIO.write(newImage, "png", new File(outputImagePath));
            graphics.dispose();

            log.info("Image with text generated at: {}", outputImagePath);
            return outputImagePath;
        } catch (IOException e) {
            log.error("Error adding text to image: {}", e.getMessage());
            throw new ImageGenerationException("Error adding text to image: " + e.getMessage());
        }
    }

    /**
     * Breaks the given text into lines that fit the given width.
     *
     * @param text     The text to break into lines.
     * @param font     The font to use for measuring the text.
     * @param maxWidth The maximum width of the text.
     * @return The lines of the text.
     */
    private static String[] breakTextIntoLines(String text, Font font, int maxWidth) {
        AttributedString attributedString = new AttributedString(text);
        attributedString.addAttribute(TextAttribute.FONT, font);

        FontRenderContext fontRenderContext = new FontRenderContext(null, true, true);
        LineBreakMeasurer lineBreakMeasurer = new LineBreakMeasurer(attributedString.getIterator(), fontRenderContext);

        List<String> lines = new ArrayList<>();

        while (lineBreakMeasurer.getPosition() < text.length()) {
            int nextOffset = lineBreakMeasurer.nextOffset(maxWidth);
            lines.add(text.substring(lineBreakMeasurer.getPosition(), nextOffset));
            lineBreakMeasurer.setPosition(nextOffset);
        }

        return lines.toArray(new String[0]);
    }

}