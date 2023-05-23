package com.swugether.server.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.swugether.server.db.domain.User;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class GoogleOAuth {
  private String CLIENT_ID = System.getenv("GOOGLE_OAUTH_CLIENT_ID");
  @Value("${GOOGLE_OAUTH_CLIENT_SECERT}")
  private String CLIENT_SECRET;

  public User authenticate(String token) {
    try {
      HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
      GsonFactory gsonFactory = GsonFactory.getDefaultInstance();
      GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, gsonFactory)
          .setAudience(Collections.singletonList(CLIENT_ID))
          .build();

      if (token != null) {
        GoogleIdToken idToken = null;

        try {
          idToken = verifier.verify(token);

          if (idToken != null) {
            Payload payload = idToken.getPayload();

            // Get profile information from payload
            String email = payload.getEmail();

            return new User(email);
          } else {
            log.error("Invalid ID token.");
          }
        } catch (GeneralSecurityException e) {
          log.info(e.getLocalizedMessage());
        } catch (IOException e) {
          log.info(e.getLocalizedMessage());
        } catch (Exception e) {
          log.info(e.getLocalizedMessage());
        }
      }

      return null;
    } catch (GeneralSecurityException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
