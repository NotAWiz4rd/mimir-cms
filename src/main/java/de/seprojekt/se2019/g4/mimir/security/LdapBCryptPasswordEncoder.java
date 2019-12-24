package de.seprojekt.se2019.g4.mimir.security;

import java.util.Base64;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class LdapBCryptPasswordEncoder implements PasswordEncoder {

  final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

  @Override
  public String encode(CharSequence charSequence) {
    return Base64.getEncoder().encodeToString(bcrypt.encode(charSequence).getBytes());
  }

  @Override
  public boolean matches(CharSequence raw, String encodedBase64) {
    return bcrypt.matches(raw, new String(Base64.getDecoder().decode(encodedBase64)));
  }
}
