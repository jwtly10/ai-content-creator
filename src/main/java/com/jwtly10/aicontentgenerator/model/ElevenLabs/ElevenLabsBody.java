package com.jwtly10.aicontentgenerator.model.ElevenLabs;

import lombok.Data;
import org.json.JSONObject;

@Data
public class ElevenLabsBody {
    private String model_id;
    private String text;
    private JSONObject voice_settings;
}
