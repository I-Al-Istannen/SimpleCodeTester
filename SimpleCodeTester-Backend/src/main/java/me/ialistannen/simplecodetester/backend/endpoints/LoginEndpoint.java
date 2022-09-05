package me.ialistannen.simplecodetester.backend.endpoints;

import jakarta.validation.constraints.NotEmpty;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import me.ialistannen.simplecodetester.backend.security.JwtGenerator;
import me.ialistannen.simplecodetester.backend.services.user.UserService;
import me.ialistannen.simplecodetester.backend.util.ResponseUtil;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.lang.JoseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class LoginEndpoint {

  private static final String CREDENTIALS_INCORRECT = "Username or password incorrect";

  private final JwtGenerator jwtGenerator;
  private final UserService userService;
  private final PasswordEncoder passwordEncoder;
  private final JwtConsumer jwtConsumer;

  public LoginEndpoint(JwtGenerator jwtGenerator, UserService userService,
      PasswordEncoder passwordEncoder, JwtConsumer jwtConsumer) {
    this.jwtGenerator = jwtGenerator;
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
    this.jwtConsumer = jwtConsumer;
  }

  @PostMapping("/login")
  public ResponseEntity<Object> login(@RequestParam @NotEmpty String username,
      @RequestParam @NotEmpty String password) {
    User user = userService.getUser(username).orElse(null);

    if (user == null) {
      return ResponseUtil.error(HttpStatus.NOT_FOUND, CREDENTIALS_INCORRECT);
    }

    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      return ResponseUtil.error(HttpStatus.UNAUTHORIZED, CREDENTIALS_INCORRECT);
    }

    JwtClaims claims = new JwtClaims();
    claims.setIssuer("SimpleCodeTester");
    claims.setSubject(user.getId());
    claims.setExpirationTimeMinutesInTheFuture(TimeUnit.DAYS.toMinutes(2));
    claims.setNotBeforeMinutesInThePast(1);
    claims.setGeneratedJwtId();

    JsonWebSignature signature = jwtGenerator.getSignature(claims);
    try {
      log.info("Logged in id {} ({})", user.getId(), user.getName());
      return ResponseEntity.ok(Map.of("token", signature.getCompactSerialization()));
    } catch (JoseException e) {
      log.warn("Error building JWT", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping("/login/get-access-token")
  public ResponseEntity<Object> getAccessToken(@RequestParam @NotEmpty String refreshToken) {
    try {
      JwtClaims claims = jwtConsumer.processToClaims(refreshToken);
      String subject = claims.getSubject();

      Optional<User> user = userService.getUser(subject);

      if (user.isEmpty()) {
        return ResponseUtil.error(HttpStatus.NOT_FOUND, "User not found");
      }

      if (!user.get().isEnabled()) {
        return ResponseUtil.error(HttpStatus.FORBIDDEN, "Account locked");
      }

      log.info("Refreshed token for id {} ({}).", user.get().getId(), user.get().getName());

      return ResponseEntity.ok(
          Map.of(
              "token", generateAccessToken(user.get()).getCompactSerialization(),
              "displayName", user.get().getName(),
              "userName", user.get().getId(),
              "roles", user.get().getAuthorities()
          )
      );
    } catch (JoseException | MalformedClaimException | InvalidJwtException e) {
      log.info("Got invalid refresh attempt for {}", refreshToken, e);
      return ResponseUtil.error(HttpStatus.UNAUTHORIZED, "Invalid JWT. Try refreshing the page");
    }
  }

  private JsonWebSignature generateAccessToken(User user) {
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("SimpleCodeTester");
    claims.setSubject(user.getId());
    claims.setStringListClaim("roles", user.getAuthorities());
    claims.setClaim("enabled", user.isEnabled());
    claims.setExpirationTimeMinutesInTheFuture(1);
    claims.setNotBeforeMinutesInThePast(1);
    claims.setGeneratedJwtId();

    return jwtGenerator.getSignature(claims);
  }
}
