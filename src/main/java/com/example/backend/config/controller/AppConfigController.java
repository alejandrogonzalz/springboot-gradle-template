package com.example.backend.config.controller;

import com.example.backend.common.BaseResponse;
import com.example.backend.external.service.ExchangeRateService;
import java.math.BigDecimal;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class AppConfigController {

  private final ExchangeRateService exchangeRateService;

  @GetMapping("/global")
  public ResponseEntity<BaseResponse<Map<String, Object>>> getGlobalConfig() {
    BigDecimal exchangeRate = exchangeRateService.getCurrentUsdMxnRate().orElse(BigDecimal.ZERO);

    Map<String, Object> config =
        Map.of(
            "exchangeRate",
            Map.of(
                "usdToMxn",
                exchangeRate,
                "mxnToUsd",
                exchangeRate.compareTo(BigDecimal.ZERO) > 0
                    ? BigDecimal.ONE.divide(exchangeRate, 4, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO));

    return ResponseEntity.ok(BaseResponse.success(config));
  }
}
