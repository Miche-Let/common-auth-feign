package com.michelet.common.auth.feign.config;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "internal.auth")
public class InternalFeignProperties {
    private String secret;
    private Feign feign = new Feign();

    @PostConstruct
    public void validate() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("internal.auth.secret must be configured.");
        }
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Feign getFeign() {
        return feign;
    }

    public void setFeign(Feign feign) {
        this.feign = feign;
    }

    public static class Feign {
        private Map<String, String> audiences = new HashMap<>();

        public Map<String, String> getAudiences() {
            return audiences;
        }

        public void setAudiences(Map<String, String> audiences) {
            this.audiences = audiences;
        }
    }
}
