package com.turing.google_oauth.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenIdUser {
    @JsonProperty("email")
    public String email;
    @JsonProperty("email_verified")
    public boolean verified;
    @JsonProperty("given_name")
    public String firstName;
    @JsonProperty("family_name")
    public String lastName;
    @JsonProperty("picture")
    public String profilePicturePath;
    public String accessToken;
    public String refreshToken;
}
