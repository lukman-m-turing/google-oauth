package com.turing.google_oauth.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.turing.google_oauth.util.Constants.*;

@Service
public class AuthService {

    @Value("${google.authorization.endpoint}")
    String authorizationEndpoint;
    @Value("${google.oauth.client.id}")
    String clientId;
    @Value("${google.oauth.client.secret}")
    String clientSecret;
    @Value("${google.oauth.client.redirect-url}")
    String redirectUrl;

    public URI getCodeFlowInitializationUrl() {
        String oauth2Scopes = getOAuth2Scopes();
        URI authorizationCodeFlowUrl = UriComponentsBuilder.fromUri(URI.create(authorizationEndpoint))
                .queryParam(CLIENT_ID_LITERAL, clientId)
                .queryParam(REDIRECT_URI_LITERAL, redirectUrl)
                .queryParam(RESPONSE_TYPE_LITERAL, "code")
                .queryParam(SCOPE_LITERAL, oauth2Scopes)
                .build()
                .toUri();
        return authorizationCodeFlowUrl;
    }

    public URI handleAuthCodeFlowCallback(String code) {
        return URI.create(PROFILE_URL);
    }

    private String getOAuth2Scopes() {
        List<String> scopes = new ArrayList<>();
        scopes.add("openid");
        scopes.add("https://www.googleapis.com/auth/userinfo.email");
        scopes.add("https://www.googleapis.com/auth/userinfo.profile");
        return String.join("+", scopes);
    }

}
