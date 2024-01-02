package com.jwtly10.aicontentgenerator.service;

import com.jwtly10.aicontentgenerator.exceptions.AudioGenerationException;
import com.jwtly10.aicontentgenerator.model.Gender;

public interface VoiceGenerator {
    /**
     * Generate voice from text
     *
     * @param text  text to generate voice from
     * @param voice voice to use
     * @return Optional path to generated voice, empty if failed
     */
    String generateVoice(String text, Gender voice, String fileId) throws AudioGenerationException;
}
