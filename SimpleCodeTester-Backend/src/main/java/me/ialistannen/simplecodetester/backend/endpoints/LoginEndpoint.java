package me.ialistannen.simplecodetester.backend.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import javax.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import me.ialistannen.simplecodetester.backend.security.JwtGenerator;
import me.ialistannen.simplecodetester.backend.services.user.UserService;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
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
  private ObjectMapper objectMapper;

  public LoginEndpoint(JwtGenerator jwtGenerator, UserService userService,
      PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
    this.jwtGenerator = jwtGenerator;
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
    this.objectMapper = objectMapper;
  }

  @PostMapping("/login")
  public ResponseEntity<String> login(@RequestParam @NotEmpty String username,
      @RequestParam @NotEmpty String password) {
    User user = userService.getUser(username).orElse(null);

    if (user == null) {
      return ResponseEntity.notFound().build();
    }

    if (!passwordEncoder.matches(password, user.getPasswordHash())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
    }

    JwtClaims claims = new JwtClaims();
    claims.setIssuer("SimpleCodeTester");
    claims.setSubject(user.getName());
    claims.setStringListClaim("roles", user.getAuthorities());
    claims.setClaim("enabled", user.getEnabled());
    claims.setExpirationTimeMinutesInTheFuture(30);
    claims.setNotBeforeMinutesInThePast(1);
    claims.setGeneratedJwtId();

    JsonWebSignature signature = jwtGenerator.getSignature(claims);
    try {
      return ResponseEntity.ok(
          objectMapper.writeValueAsString(Map.of("token", signature.getCompactSerialization()))
      );
    } catch (JoseException | JsonProcessingException e) {
      log.warn("Error building JWT", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
