package com.turing.google_oauth.util;

import org.apache.tomcat.util.codec.binary.Base64;

public class JwtUtils {

    public static String getClaims(String rawToken) {
        String[] tokenParts = rawToken.split("\\.");
        return new String(Base64.decodeBase64(tokenParts[1]));
    }

}
