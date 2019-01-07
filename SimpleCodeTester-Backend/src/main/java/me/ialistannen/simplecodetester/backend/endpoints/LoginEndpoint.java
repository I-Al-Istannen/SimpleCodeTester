package me.ialistannen.simplecodetester.backend.endpoints;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotEmpty;
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

  private JwtGenerator jwtGenerator;
  private UserService userService;
  private PasswordEncoder passwordEncoder;
  private JwtConsumer jwtConsumer;

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
      return ResponseUtil.error(HttpStatus.NOT_FOUND, "Username not found");
    }

    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      return ResponseUtil.error(HttpStatus.UNAUTHORIZED, "Invalid password");
    }

    JwtClaims claims = new JwtClaims();
    claims.setIssuer("SimpleCodeTester");
    claims.setSubject(user.getId());
    claims.setExpirationTimeMinutesInTheFuture(TimeUnit.DAYS.toMinutes(2));
    claims.setNotBeforeMinutesInThePast(1);
    claims.setGeneratedJwtId();

    JsonWebSignature signature = jwtGenerator.getSignature(claims);
    try {
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

      if (!user.get().getEnabled()) {
        return ResponseUtil.error(HttpStatus.FORBIDDEN, "Account locked");
      }

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
      return ResponseUtil.error(HttpStatus.UNAUTHORIZED, "Invalid JWT");
    }
  }

  private JsonWebSignature generateAccessToken(User user) {
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("SimpleCodeTester");
    claims.setSubject(user.getId());
    claims.setStringListClaim("roles", user.getAuthorities());
    claims.setClaim("enabled", user.getEnabled());
    claims.setExpirationTimeMinutesInTheFuture(1);
    claims.setNotBeforeMinutesInThePast(1);
    claims.setGeneratedJwtId();

    return jwtGenerator.getSignature(claims);
  }
}
