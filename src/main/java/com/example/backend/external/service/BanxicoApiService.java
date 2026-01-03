package com.example.backend.external.service;

import com.example.backend.external.config.ExternalApiConfig.BanxicoApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class BanxicoApiService {

  private final RestTemplate restTemplate;
  private final BanxicoApiProperties banxicoProperties;

  public String getSeriesDataByDateRange(String series, String startDate, String endDate) {
    String url =
        UriComponentsBuilder.fromHttpUrl(banxicoProperties.getBaseUrl())
            .path("/series/{series}/datos/{startDate}/{endDate}")
            .buildAndExpand(series, startDate, endDate)
            .toUriString();

    HttpHeaders headers = new HttpHeaders();
    headers.set("Bmx-Token", banxicoProperties.getToken());
    HttpEntity<String> entity = new HttpEntity<>(headers);

    log.info("Calling Banxico API: {}", url);

    return restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
  }
}
