package me.shaposhnik.hlrbot.integration.bsg;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DefaultReferenceGenerator implements ReferenceGenerator {

  @Override
  public String generateReference() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 13);
  }
}
