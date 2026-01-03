package com.example.backend.external.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ExternalApiConfig {

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  @ConfigurationProperties(prefix = "external.api.banxico")
  public BanxicoApiProperties banxicoApiProperties() {
    return new BanxicoApiProperties();
  }

  public static class BanxicoApiProperties {
    private String baseUrl = "https://www.banxico.org.mx/SieAPIRest/service/v1";
    private String token;

    public String getBaseUrl() {
      return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
    }

    public String getToken() {
      return token;
    }

    public void setToken(String token) {
      this.token = token;
    }
  }
}
