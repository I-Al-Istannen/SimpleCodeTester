package me.ialistannen.simplecodetester.backend.security;

import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletResponse;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
@Configuration
public class SecurityJavaConfig extends WebSecurityConfigurerAdapter {

  @Value("${jwt.secret.key}")
  private String jwtTokenSecret;

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  JwtConsumer getJwtConsumer() {
    return new JwtConsumerBuilder()
        .setRequireExpirationTime()
        .setAllowedClockSkewInSeconds(60)
        .setMaxFutureValidityInMinutes((int) TimeUnit.DAYS.toMinutes(2))
        .setVerificationKey(new HmacKey(jwtTokenSecret.getBytes()))
        .build();
  }

  @Bean
  JwtGenerator getJwtGenerator() {
    return new JwtGenerator(new HmacKey(jwtTokenSecret.getBytes()));
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .cors().and()
        .csrf().disable()
        .exceptionHandling()
        .authenticationEntryPoint((request, response, authException) ->
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
        )
        .and()
        .addFilterBefore(
            new JwtFilter(getJwtConsumer()),
            UsernamePasswordAuthenticationFilter.class
        )
        // don't create session
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authorizeRequests()
        .antMatchers("/login").permitAll()
        .antMatchers("/login/get-access-token").permitAll()
        .antMatchers("/admin/**").hasRole("ADMIN")
        .antMatchers("/check-category/delete/**").hasRole("ADMIN")
        .antMatchers("/check-category/add-new/**").hasRole("ADMIN")
        .antMatchers("/check-category/rename/**").hasRole("ADMIN")
        .antMatchers("/checks/approve").hasRole("ADMIN")
        .anyRequest().authenticated();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource(AllowedOrigins allowedOrigins) {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedMethod(HttpMethod.OPTIONS);
    configuration.addAllowedMethod(HttpMethod.GET);
    configuration.addAllowedMethod(HttpMethod.HEAD);
    configuration.addAllowedMethod(HttpMethod.POST);
    configuration.addAllowedMethod(HttpMethod.PATCH);
    configuration.addAllowedMethod(HttpMethod.DELETE);
    configuration.addAllowedHeader("Authorization");
    configuration.addAllowedHeader("Content-Type");
    configuration.setAllowedOrigins(allowedOrigins.getAllowedOrigins());
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Component
  @ConfigurationProperties("cors")
  @lombok.Value
  public static class AllowedOrigins {

    private List<String> allowedOrigins;
  }
}
