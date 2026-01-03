package com.example.backend.common.utils;

import java.time.Instant;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@Named("DateMappingUtils")
public class DateMappingUtils {

  @Named("toStartOfDay")
  public Instant toStartOfDay(String dateStr) {
    if (dateStr == null || dateStr.isBlank()) return null;
    return DateUtils.parseFlexibleDate(dateStr, getTimezone());
  }

  @Named("toEndOfDay")
  public Instant toEndOfDay(String dateStr) {
    if (dateStr == null || dateStr.isBlank()) return null;
    return DateUtils.parseFlexibleDateEndOfDay(dateStr, getTimezone());
  }

  private String getTimezone() {
    ServletRequestAttributes attrs =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    return (attrs != null) ? attrs.getRequest().getHeader("X-Timezone") : null;
  }
}
