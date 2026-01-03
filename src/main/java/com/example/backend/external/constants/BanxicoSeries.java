package com.example.backend.external.constants;

public enum BanxicoSeries {
  /**
   * Tipo de cambio pesos por dólar E.U.A., Tipo de cambio para solventar obligaciones denominadas
   * en moneda extranjera, Fecha de liquidación
   */
  SF60653("SF60653"),
  /**
   * Tipo de cambio Pesos por dólar E.U.A., Tipo de cambio para solventar obligaciones denominadas
   * en moneda extranjera, Fecha de determinación (FIX)
   */
  SF43718("SF43718"),
  SF43787("SF43787"),
  SF43784("SF43784"),
  SF43788("SF43788"),
  SF43786("SF43786"),
  SF43785("SF43785"),
  SF43717("SF43717"),
  SF63528("SF63528"),
  SF343410("SF343410");

  private final String code;

  BanxicoSeries(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public static String formatSeriesList(BanxicoSeries... series) {
    return String.join(
        ",", java.util.Arrays.stream(series).map(BanxicoSeries::getCode).toArray(String[]::new));
  }

  public static String formatSeriesRange(BanxicoSeries start, BanxicoSeries end) {
    return start.getCode() + "-" + end.getCode();
  }
}
