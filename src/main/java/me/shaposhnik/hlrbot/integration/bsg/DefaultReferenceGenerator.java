package me.shaposhnik.hlrbot.integration.bsg;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DefaultReferenceGenerator implements ReferenceGenerator {

    @Override
    public String generateReference() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 13);
    }
}
