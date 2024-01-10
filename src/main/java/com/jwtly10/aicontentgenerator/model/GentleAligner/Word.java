package com.jwtly10.aicontentgenerator.model.GentleAligner;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Word */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Word {
    @JsonProperty("alignedWord")
    private String alignedWord;

    @JsonProperty("start")
    private double start;

    @JsonProperty("end")
    private double end;

    @JsonProperty("word")
    private String originalWord;

    private String wordWithPunc;
}
