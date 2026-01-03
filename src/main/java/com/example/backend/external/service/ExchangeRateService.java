package com.example.backend.external.service;

import com.example.backend.audit.aop.Auditable;
import com.example.backend.external.constants.BanxicoSeries;
import com.example.backend.external.entity.CurrentExchangeRate;
import com.example.backend.external.repository.CurrentExchangeRateRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateService {

  private final CurrentExchangeRateRepository exchangeRateRepository;
  private final BanxicoApiService banxicoApiService;
  private final ObjectMapper objectMapper;

  public Optional<BigDecimal> getCurrentUsdMxnRate() {
    return exchangeRateRepository
        .findBySeriesId(BanxicoSeries.SF43718.getCode())
        .map(CurrentExchangeRate::getRateValue);
  }

  @EventListener(ApplicationReadyEvent.class) // Run on application start
  @Scheduled(cron = "${external.scheduler.exchange-rate.cron}")
  @Auditable(
      operation = "UPDATE_EXCHANGE_RATE",
      entityType = "EXCHANGE_RATE",
      description = "Update current USD-MXN exchange rate")
  public CurrentExchangeRate updateCurrentExchangeRate() {
    log.info("Starting scheduled update for USD-MXN exchange rate...");

    try {
      // We need the exchange rate from yesterday, if we calculate now it'll be 'null'
      LocalDate yesterday = LocalDate.now().minusDays(1);
      String seriesCode = BanxicoSeries.SF43718.getCode();
      String response =
          banxicoApiService.getSeriesDataByDateRange(
              seriesCode, yesterday.toString(), yesterday.toString());

      log.debug("Banxico API response: {}", response);

      JsonNode root = objectMapper.readTree(response);
      JsonNode seriesArray = root.path("bmx").path("series");

      // Safety check: ensure the response contains data
      if (seriesArray.isMissingNode() || seriesArray.get(0).path("datos").isEmpty()) {
        log.warn("No exchange rate data found in Banxico response for date: {}", yesterday);
        return null;
      }

      JsonNode data = seriesArray.get(0).path("datos").get(0);
      BigDecimal rate = new BigDecimal(data.path("dato").asText());
      LocalDate date =
          LocalDate.parse(
              data.path("fecha").asText(),
              java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));

      log.debug("Parsed rate: {} for date: {}", rate, date);

      CurrentExchangeRate exchangeRate =
          exchangeRateRepository
              .findBySeriesId(seriesCode)
              .orElseGet(
                  () -> {
                    log.info(
                        "No existing record found for series {}. Creating new entry.", seriesCode);
                    return new CurrentExchangeRate();
                  });

      exchangeRate.setSeriesId(seriesCode);
      exchangeRate.setRateDate(date);
      exchangeRate.setRateValue(rate);

      CurrentExchangeRate savedRate = exchangeRateRepository.save(exchangeRate);
      log.info(
          "Successfully updated exchange rate (ID: {}) to {} for date {}",
          savedRate.getId(),
          rate,
          date);
      return savedRate;

    } catch (JsonProcessingException e) {
      log.error("JSON parsing error while processing Banxico response: {}", e.getMessage());
    } catch (Exception e) {
      log.error("Unexpected error during exchange rate update: ", e);
    }
    return null;
  }
}
