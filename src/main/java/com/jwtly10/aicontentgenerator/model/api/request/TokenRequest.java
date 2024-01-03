package com.jwtly10.aicontentgenerator.model.api.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TokenRequest {
    private String token;

    @JsonCreator
    public TokenRequest(@JsonProperty("token") String token) {
        this.token = token;
    }
}
