package com.jwtly10.aicontentgenerator.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/** Word */
@Data
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
}
