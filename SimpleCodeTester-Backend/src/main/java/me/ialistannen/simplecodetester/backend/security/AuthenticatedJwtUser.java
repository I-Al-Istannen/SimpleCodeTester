package me.ialistannen.simplecodetester.backend.security;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.ToString;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * A user that is authenticated by a JWT:
 */
@ToString
public class AuthenticatedJwtUser implements UserDetails {

  private final JwtClaims claims;
  private final String principal;
  private final List<GrantedAuthority> authorities;
  private final boolean enabled;

  /**
   * Creates a new AuthenticatedJwtUser from the given {@link JwtClaims}.
   *
   * @param claims the claims to build from
   * @throws MalformedClaimException if the claims were malformed
   */
  AuthenticatedJwtUser(JwtClaims claims) throws MalformedClaimException {
    this.claims = claims;
    this.principal = claims.getSubject();
    this.authorities = claims.getStringListClaimValue("roles").stream()
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());
    this.enabled = (boolean) claims.getClaimValue("enabled");
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return principal;
  }

  @Override
  public boolean isAccountNonExpired() {
    // JWT was valid and the auth is stateless
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    // JWT was valid and the auth is stateless
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    // JWT was valid and the auth is stateless
    return true;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }
}
