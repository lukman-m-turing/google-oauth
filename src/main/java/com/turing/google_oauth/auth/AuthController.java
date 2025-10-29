package com.turing.google_oauth.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/initialize-flow")
    public ResponseEntity<Void> initializeAuthorizationCodeFlow() {
        URI authorizationCodeFlowUrl = authService.getCodeFlowInitializationUrl();
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(authorizationCodeFlowUrl)
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> handleAuthorizationCodeFlowCallback(
            @RequestParam("code") String code
    ) {
        URI profilePageUrl = authService.handleAuthCodeFlowCallback(code);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(profilePageUrl)
                .build();
    }

}
