package me.ialistannen.simplecodetester.backend.security;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * A {@link javax.servlet.Filter} that logs the user in based on a JWT:
 */
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

  private static final String PREFIX = "Bearer ";
  private JwtConsumer jwtConsumer;

  /**
   * Creates a new JwtFilter with the given {@link JwtConsumer}.
   *
   * @param jwtConsumer the {@link JwtConsumer} to use
   */
  JwtFilter(JwtConsumer jwtConsumer) {
    this.jwtConsumer = jwtConsumer;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {

    String requestHeader = request.getHeader("Authorization");

    if (requestHeader == null || !requestHeader.startsWith(PREFIX)) {
      logger.info("Invalid auth received, no header present");
      chain.doFilter(request, response);
      return;
    }
    String token = requestHeader.substring(PREFIX.length()).trim();

    // already authenticated
    if (SecurityContextHolder.getContext().getAuthentication() != null) {
      log.info(
          "Already authenticated: '{}'",
          SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()
      );
      chain.doFilter(request, response);
      return;
    }

    log.info("Security context did not exist, authorizing user.");

    try {
      JwtClaims jwtClaims = jwtConsumer.processToClaims(token);

      AuthenticatedJwtUser authenticatedUser = new AuthenticatedJwtUser(jwtClaims);

      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          authenticatedUser, null, authenticatedUser.getAuthorities()
      );
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      log.info("Authorized user '{}', setting security context", authenticatedUser.getUsername());
      SecurityContextHolder.getContext().setAuthentication(authentication);
    } catch (InvalidJwtException | MalformedClaimException e) {
      log.info("JWT is no longer valid for '{}'", token);
    }

    chain.doFilter(request, response);
  }
}
