package me.ialistannen.simplecodetester.backend.endpoints;

import javax.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import me.ialistannen.simplecodetester.backend.db.entities.User;
import me.ialistannen.simplecodetester.backend.db.repos.UserRepository;
import me.ialistannen.simplecodetester.backend.security.JwtGenerator;
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
  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;

  public LoginEndpoint(JwtGenerator jwtGenerator, UserRepository userRepository,
      PasswordEncoder passwordEncoder) {
    this.jwtGenerator = jwtGenerator;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @PostMapping("/login")
  public ResponseEntity<String> login(@RequestParam @NotEmpty String username,
      @RequestParam @NotEmpty String password) {
    User user = userRepository.findById(username).orElse(null);

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
    claims.setExpirationTimeMinutesInTheFuture(30);
    claims.setNotBeforeMinutesInThePast(1);
    claims.setGeneratedJwtId();

    JsonWebSignature signature = jwtGenerator.getSignature(claims);
    try {
      return ResponseEntity.ok(signature.getCompactSerialization());
    } catch (JoseException e) {
      log.warn("Error building JWT", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
