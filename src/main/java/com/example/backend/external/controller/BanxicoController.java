package com.example.backend.external.controller;

import com.example.backend.common.BaseResponse;
import com.example.backend.external.service.BanxicoApiService;
import com.example.backend.external.service.ExchangeRateService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/external/banxico")
@RequiredArgsConstructor
public class BanxicoController {

  private final BanxicoApiService banxicoApiService;
  private final ExchangeRateService exchangeRateService;

  @GetMapping("/series/{series}/dates/{startDate}/{endDate}")
  public ResponseEntity<BaseResponse<String>> getSeriesDataByDateRange(
      @PathVariable String series, @PathVariable String startDate, @PathVariable String endDate) {
    String data = banxicoApiService.getSeriesDataByDateRange(series, startDate, endDate);
    return ResponseEntity.ok(BaseResponse.success(data));
  }

  @GetMapping("/exchange-rate/usd-mxn")
  public ResponseEntity<BaseResponse<BigDecimal>> getCurrentUsdMxnRate() {
    return exchangeRateService
        .getCurrentUsdMxnRate()
        .map(rate -> ResponseEntity.ok(BaseResponse.success(rate)))
        .orElse(ResponseEntity.ok(BaseResponse.error("Exchange rate not found")));
  }

  @PostMapping("/exchange-rate/refresh")
  public ResponseEntity<BaseResponse<String>> refreshExchangeRate() {
    try {
      exchangeRateService.updateCurrentExchangeRate();
      return ResponseEntity.ok(BaseResponse.success("Exchange rate refresh triggered"));
    } catch (Exception e) {
      return ResponseEntity.ok(BaseResponse.error("Failed to refresh: " + e.getMessage()));
    }
  }
}
