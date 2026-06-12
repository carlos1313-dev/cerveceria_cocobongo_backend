package com.cocobongo.cerveceria.inventory.dto;
 
import com.cocobongo.cerveceria.inventory.entities.ProductType;
import lombok.*;
 
import java.math.BigDecimal;
 
/**
 * Respuesta de producto con valores en ambas monedas.
 *
 * Los campos en VES se calculan en ProductService usando CurrencyConverter
 * y la tasa BCV vigente al momento de la consulta.
 *
 * El frontend alterna entre vistas USD/VES sin llamadas adicionales —
 * ambos valores siempre vienen en la respuesta.
 *
 * suggestedPriceVes = CurrencyConverter.calculateSellingPriceVes(cost, rate)
 *   → valor por defecto que el usuario puede sobreescribir al editar el producto
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
 
    private Integer     idProduct;
    private Integer     providerId;
    private String      providerName;
    private String      name;
    private String      description;
    private ProductType type;
    private Boolean     isActive;
 
    // ── Valores en USD (moneda base — siempre presentes) ─────────────────────
    private BigDecimal cost;     // costo de compra en USD
    private BigDecimal price;    // precio de venta en USD
 
    // ── Valores en VES (calculados con tasa BCV del momento) ─────────────────
    private BigDecimal costVes;           // cost * rate
    private BigDecimal priceVes;          // price * rate
    private BigDecimal suggestedPriceVes; // fórmula: cost*1.30*(rate+5) redondeado a 10↑
 
    // Tasa usada para los cálculos — útil para que el frontend sepa
    // con qué tasa se calcularon los valores VES mostrados
    private BigDecimal exchangeRate;
}