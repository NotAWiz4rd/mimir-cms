package de.seprojekt.se2019.g4.mimir.security;

import java.io.IOException;
import java.util.Collections;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

  private JwtTokenProvider jwtTokenProvider;

  public JwtAuthorizationFilter(AuthenticationManager authenticationManager,
      ApplicationContext ctx) {
    super(authenticationManager);
    this.jwtTokenProvider = ctx.getBean(JwtTokenProvider.class);
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {
    var authentication = getAuthentication(request);
    if (authentication == null) {
      filterChain.doFilter(request, response);
      return;
    }

    SecurityContextHolder.getContext().setAuthentication(authentication);
    filterChain.doFilter(request, response);
  }

  private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
    String token;

    if(request.getRequestURI().matches("/(artifact|folder)/[\\d]+/download")) {
      token = request.getParameter("token");

      if (token == null) {
        return null;
      }
    } else {
      token = request.getHeader("Authorization");

      if (token == null) {
        return null;
      }

      if (!token.startsWith("Bearer ")) {
        return null;
      }

      token = token.replace("Bearer ", "");
    }

    if (!jwtTokenProvider.validateToken(token)) {
      return null;
    }

    var username = jwtTokenProvider.getPayload(token, "sub");
    if (username.equals(JwtPrincipal.shareLinkUserName)) {
      var sharedEntityId = Long.parseLong(jwtTokenProvider.getPayload(token, "id"));
      var sharedEntityType = jwtTokenProvider.getPayload(token, "type");
      return new UsernamePasswordAuthenticationToken(
          new JwtPrincipal(username, sharedEntityId, sharedEntityType), null,
          Collections.emptyList());
    } else {
      return new UsernamePasswordAuthenticationToken(new JwtPrincipal(username), null,
          Collections.emptyList());
    }
  }
}
