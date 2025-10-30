package com.turing.google_oauth.auth.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OpenIdResponse {
    @JsonProperty("id_token")
    public String identityToken;
    @JsonProperty("access_token")
    public String accessToken;
    @JsonProperty("token_type")
    public String tokenType; //Bearer
}
