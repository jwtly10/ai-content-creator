package com.jwtly10.aicontentgenerator.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

/** GentleResponse */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GentleResponse {
    @JsonProperty("words")
    private List<Word> words;
}
