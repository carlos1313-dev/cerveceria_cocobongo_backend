package com.cocobongo.cerveceria.common.utils;
 
import java.math.BigDecimal;
import java.math.RoundingMode;
 
/**
 * Utilitaria de conversión de monedas y cálculo de precios en bolívares.
 *
 * Todos los métodos son estáticos — no necesita estado ni contexto de Spring.
 * Puede usarse desde cualquier servicio sin inyección.
 *
 * Convenciones:
 *   - USD = dólares (moneda base del sistema)
 *   - VES = bolívares venezolanos
 *   - rate = tasa BCV vigente (bolívares por 1 USD). Ej: 567.68
 */
public final class CurrencyConverter {
 
    private CurrencyConverter() {}
 
    // ── Conversiones básicas ──────────────────────────────────────────────────
 
    /**
     * Convierte un monto en USD a VES usando la tasa dada.
     * resultado = usd * rate
     */
    public static BigDecimal usdToVes(BigDecimal usd, BigDecimal rate) {
        return usd.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
 
    /**
     * Convierte un monto en VES a USD usando la tasa dada.
     * resultado = ves / rate
     */
    public static BigDecimal vesToUsd(BigDecimal ves, BigDecimal rate) {
        return ves.divide(rate, 6, RoundingMode.HALF_UP)
                  .setScale(2, RoundingMode.HALF_UP);
    }
 
    // ── Fórmula de precio de venta en bolívares ───────────────────────────────
 
    /**
     * Calcula el precio de venta sugerido en bolívares a partir del costo en USD.
     *
     * Fórmula:
     *   precioVentaUsd  = costUsd * 1.30               (margen del 30%)
     *   precioVentaVes  = precioVentaUsd * (rate + 5)  (tasa BCV + 5 de spread)
     *   resultado       = redondear al múltiplo de 10 hacia arriba más cercano
     *
     * Ejemplo:
     *   costUsd = 1.00, rate = 567.68
     *   precioVentaUsd  = 1.00 * 1.30 = 1.30
     *   precioVentaVes  = 1.30 * (567.68 + 5) = 1.30 * 572.68 = 744.484
     *   redondeado      = 750  (múltiplo de 10 hacia arriba)
     *
     * @param costUsd costo del producto en USD
     * @param rate    tasa BCV vigente (bolívares por 1 USD)
     * @return precio de venta sugerido en VES, redondeado al múltiplo de 10 ↑
     */
    public static BigDecimal calculateSellingPriceVes(BigDecimal priceUsd,
                                                       BigDecimal rate) {

        if (priceUsd == null || rate == null) return BigDecimal.ZERO;

        //BigDecimal sellingUsd = costUsd.multiply(new BigDecimal("1.30"));
 
        // 2. Convertir a bolívares con spread de 5 sobre la tasa
        BigDecimal rateWithSpread = rate.add(new BigDecimal("5"));
        BigDecimal rawVes = priceUsd.multiply(rateWithSpread);
 
        // 3. Redondear al múltiplo de 10 hacia arriba más cercano
        return roundUpToNearest10(rawVes);
    }
 
    /**
     * Redondea un valor al múltiplo de 10 hacia arriba más cercano.
     *
     * Ejemplos:
     *   744.484 → 750
     *   740.000 → 740  (ya es múltiplo exacto)
     *   741.001 → 750
     *   800.000 → 800
     */
    public static BigDecimal roundUpToNearest10(BigDecimal value) {
        // Dividir entre 10, redondear hacia arriba (CEILING), multiplicar por 10
        BigDecimal divided  = value.divide(BigDecimal.TEN, 0, RoundingMode.CEILING);
        return divided.multiply(BigDecimal.TEN);
    }
 
    // ── Precio de venta en USD ────────────────────────────────────────────────
 
    /**
     * Precio de venta sugerido en USD (sin conversión a bolívares).
     * precioVentaUsd = costUsd * 1.30
     */
    public static BigDecimal calculateSellingPriceUsd(BigDecimal costUsd) {
        return costUsd.multiply(new BigDecimal("1.30"))
                      .setScale(2, RoundingMode.HALF_UP);
    }
}