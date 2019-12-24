package de.seprojekt.se2019.g4.mimir.security;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Base64.Decoder;
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
  public boolean matches(CharSequence rawBase64, String encodedBase64) {
    Decoder decoder = Base64.getDecoder();
    var raw = new String(decoder.decode(rawBase64.toString()), Charset.forName("UTF-8"));
    var encoded = new String(decoder.decode(encodedBase64));
    return bcrypt.matches(raw, encoded);
  }
}
