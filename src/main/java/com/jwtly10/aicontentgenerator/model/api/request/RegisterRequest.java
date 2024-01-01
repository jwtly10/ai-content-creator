package com.jwtly10.aicontentgenerator.model.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterRequest {
    private String email;
    private String password;
    private String firstname;
    private String lastname;
}
