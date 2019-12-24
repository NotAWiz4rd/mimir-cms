package de.seprojekt.se2019.g4.mimir.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private final AuthenticationManager authenticationManager;

  private JwtTokenProvider jwtTokenProvider;

  public JwtAuthenticationFilter(AuthenticationManager authenticationManager,
      ApplicationContext ctx) {
    this.authenticationManager = authenticationManager;
    setFilterProcessesUrl("/login");
    this.jwtTokenProvider = ctx.getBean(JwtTokenProvider.class);
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) {
    var username = request.getParameter("username");
    var password = request.getParameter("password");
    if (password != null) {
      password = Base64.getEncoder().encodeToString(password.getBytes());
    }
    var authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

    return authenticationManager.authenticate(authenticationToken);
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain, Authentication authentication) throws IOException {
    var token = jwtTokenProvider.generateToken(authentication);
    var om = new ObjectMapper();
    var map = new HashMap<String, String>();
    map.put("token", token);
    var json = om.writeValueAsString(map);
    response.getWriter().write(json);
    response.flushBuffer();
  }
}
