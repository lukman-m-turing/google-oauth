package com.turing.google_oauth.auth.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OpenIdResponse {
    @JsonProperty("id_token")
    public String identityToken;
    @JsonProperty("token_type")
    public String tokenType; //Bearer
}
