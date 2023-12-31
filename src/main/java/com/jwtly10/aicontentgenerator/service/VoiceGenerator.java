package com.jwtly10.aicontentgenerator.service;

import com.jwtly10.aicontentgenerator.model.Gender;

import java.util.Optional;

public interface VoiceGenerator<T> {
    /**
     * Generate voice from text
     *
     * @param text  text to generate voice from
     * @param voice voice to use
     * @return Optional path to generated voice, empty if failed
     */
    Optional<String> generateVoice(String text, Gender voice, String fileId);
}
