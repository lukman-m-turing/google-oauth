package com.turing.google_oauth.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turing.google_oauth.auth.model.OpenIdResponse;
import com.turing.google_oauth.auth.model.OpenIdUser;
import com.turing.google_oauth.user.User;
import com.turing.google_oauth.user.UserService;
import com.turing.google_oauth.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.turing.google_oauth.util.Constants.*;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final HttpClient httpClient;

    @Value("${google.authorization.endpoint}")
    String authorizationEndpoint;
    @Value("${google.oauth.client.id}")
    String clientId;
    @Value("${google.oauth.client.secret}")
    String clientSecret;
    @Value("${google.oauth.client.redirect-url}")
    String redirectUrl;
    @Value("${google.api.endpoint}")
    String googleTokenEndpoint;


    public URI getCodeFlowInitializationUrl() {
        String oauth2Scopes = getOAuth2Scopes();
        return UriComponentsBuilder.fromUri(URI.create(authorizationEndpoint))
                .queryParam(CLIENT_ID_LITERAL, clientId)
                .queryParam(REDIRECT_URI_LITERAL, redirectUrl)
                .queryParam(RESPONSE_TYPE_LITERAL, "code")
                .queryParam(SCOPE_LITERAL, oauth2Scopes)
                .build()
                .toUri();
    }

    public URI handleAuthCodeFlowCallback(String code) {
        String formData = buildFormData(
                "client_id", clientId,
                "client_secret", clientSecret,
                "grant_type", "authorization_code",
                "code", code,
                "redirect_uri", redirectUrl);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(googleTokenEndpoint + "/token"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        HttpResponse<String> response;
        OpenIdResponse openIdResponse;
        OpenIdUser openIdUser;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            openIdResponse = objectMapper.readValue(response.body(), OpenIdResponse.class);
            String claims = JwtUtils.getClaims(openIdResponse.identityToken);
            openIdUser = objectMapper.readValue(claims, OpenIdUser.class);
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        User user;
        String weakToken = UUID.randomUUID().toString();
        if (userService.existsByEmail(openIdUser.email)) {
            user = loginExistingUser(openIdUser, weakToken);
        } else {
            user = registerNewUser(openIdUser, weakToken);
        }
        return UriComponentsBuilder.fromUriString(PROFILE_URL)
                .queryParam("token", weakToken)
                .build()
                .toUri();
    }

    public User loginExistingUser(OpenIdUser openIdUser, String weakToken) {
        User user = userService.findByEmail(openIdUser.email);
        user.weakToken = weakToken;
        userService.updateToken(user, weakToken);
        return user;
    }

    public User registerNewUser(OpenIdUser openIdUser, String weakToken) {
        User user = new User();
        user.email = openIdUser.email;
        user.firstName = openIdUser.firstName;
        user.lastName = openIdUser.lastName;
        user.profilePictureUrl = openIdUser.profilePicturePath;
        user.weakToken = weakToken;
        return userService.createUser(user);
    }

    private static String buildFormData(String... params) {
        if (params.length % 2 != 0)
            throw new IllegalArgumentException("Form parameters must be in key-value pairs");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.length; i += 2) {
            if (i > 0) sb.append("&");
            sb.append(URLEncoder.encode(params[i], StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(params[i + 1], StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    private String getOAuth2Scopes() {
        List<String> scopes = new ArrayList<>();
        scopes.add("openid");
        scopes.add("https://www.googleapis.com/auth/userinfo.email");
        scopes.add("https://www.googleapis.com/auth/userinfo.profile");
        return String.join("+", scopes);
    }

}
