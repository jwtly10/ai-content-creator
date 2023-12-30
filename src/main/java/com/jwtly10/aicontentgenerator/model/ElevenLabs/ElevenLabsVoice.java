package com.jwtly10.aicontentgenerator.model.ElevenLabs;

import lombok.Data;

@Data
public class ElevenLabsVoice {
    private String voice_id;
    private String name;
    private String category;

    private String accent;
    private String description;
    private String age;
    private String gender;
    private String use_case;
}
