package me.ialistannen.simplecodetester.backend.security;

import java.security.Key;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

/**
 * A simple class that generates JWTs.
 */
public class JwtGenerator {

  private final Key key;

  /**
   * Creates a new JwtGenerator and initializes it with a {@link Key} to use.
   *
   * @param key the {@link Key} to use to sign JWTs
   */
  public JwtGenerator(Key key) {
    this.key = key;
  }

  /**
   * Generates a new {@link JsonWebSignature} for the given {@link JwtClaims}.
   *
   * @param claims the claims
   * @return the generated {@link JsonWebSignature}
   */
  public JsonWebSignature getSignature(JwtClaims claims) {
    JsonWebSignature jsonWebSignature = new JsonWebSignature();
    jsonWebSignature.setKey(key);
    jsonWebSignature.setPayload(claims.toJson());
    jsonWebSignature.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
    return jsonWebSignature;
  }
}
