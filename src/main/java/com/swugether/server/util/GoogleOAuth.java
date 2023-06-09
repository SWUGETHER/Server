package com.swugether.server.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.swugether.server.db.domain.UserEntity;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GoogleOAuth {
  private final String CLIENT_ID = System.getenv("GOOGLE_OAUTH_CLIENT_ID");

  public UserEntity authenticate(String token) throws GeneralSecurityException, IOException {
    HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
    GsonFactory gsonFactory = GsonFactory.getDefaultInstance();
    GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, gsonFactory)
        .setAudience(Collections.singletonList(CLIENT_ID))
        .build();

    if (token != null) {

      GoogleIdToken idToken = verifier.verify(token);

      if (idToken != null) {
        Payload payload = idToken.getPayload();

        // Get profile information from payload
        String email = payload.getEmail();
        String nickname = (String) payload.get("given_name");

        return new UserEntity(email, nickname);
      } else {
        log.error("Invalid ID token.");
      }
    }

    return null;
  }
}
