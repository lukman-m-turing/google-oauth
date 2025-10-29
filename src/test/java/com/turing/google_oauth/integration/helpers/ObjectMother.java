package com.turing.google_oauth.integration.helpers;

import com.turing.google_oauth.user.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ObjectMother {

    /***
     * email for test user is always lukman.m@turing.com
     * More details can be revealed with a JWT decoder i.e. https://jwt.io
     * @return an OpenID Connect Identity Token
     */
    public String getOIDCIDToken() {
        return """
                {
                    "id_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6Ijg4NDg5MjEyMmUyOTM5ZmQxZjMxMzc1YjJiMzYzZWM4MTU3MjNiYmIiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI0Nzk2Mzc4NTE1NzUtZmw0bWRkZ2RhamJhZjNxa2ZvcnE1bmFrcTc5N29kN20uYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI0Nzk2Mzc4NTE1NzUtZmw0bWRkZ2RhamJhZjNxa2ZvcnE1bmFrcTc5N29kN20uYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDczNzM2NjczMjQzMzQ4NzYzOTciLCJoZCI6InR1cmluZy5jb20iLCJlbWFpbCI6Imx1a21hbi5tQHR1cmluZy5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXRfaGFzaCI6IlBrY0xhOFVtTGNOV0lmdEdMRWFLbkEiLCJuYW1lIjoiTHVrbWFuIE11ZGkiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EvQUNnOG9jSTN2dnQza1FfY0hRUDhES3E4S1cyZHFIWU9YMUQzMUl6eEJHLUEzMU5lRERseUpqOD1zOTYtYyIsImdpdmVuX25hbWUiOiJMdWttYW4iLCJmYW1pbHlfbmFtZSI6Ik11ZGkiLCJpYXQiOjE3NjE3MjQ5ODAsImV4cCI6MTc2MTcyODU4MH0.uKnyv75NNrw7G_XoNeK8ZRk6FixdSP41jIc2tYkFwPgTAvise4k9A4wPSg38WkN4QZ8pObpg3QFEVmBVWh5x1-shW_wGaq0eEOSm8zq_Yhwh7FZtNjiUV8BndcX8TGSVHHOS7lHyD7GICB7YR7Lg6vLLuhAcwqAaDCg_eYtDe1B1c6yicwnLJ2P8K8sCfosScP9jXRLA_j63nRvnnboMoKsdOkjfkLAdDfg_cITarl7uOJ3yhtKS03EBeEWMW8xzhymXJ3w7AObxswCD9ExYR645RbPDBm_e-ChwsO9dHUUS6LTGDpr6PNWP7DEAwq7nXjFuEoDFZCZr_DTSWY_CcQ"
                }
                """;
    }

    public static String getClientId() {
        return RandomStringUtils.secure().nextAlphanumeric(20);
    }

    public static String getClientSecret() {
        return RandomStringUtils.secure().nextAlphanumeric(40);
    }

    public User getUserFor(String email) {
        User user = new User();
        user.email = email;
        user.firstName = "Kaniel";
        user.lastName = "Outis";
        user.profilePictureUrl = "http://localhost:8080/fake-image.png";
        user.weakToken = UUID.randomUUID().toString();
        return user;
    }

}
