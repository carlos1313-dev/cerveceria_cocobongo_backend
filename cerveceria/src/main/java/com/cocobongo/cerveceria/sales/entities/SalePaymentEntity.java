package com.cocobongo.cerveceria.sales.entities;
 
import jakarta.persistence.*;
import lombok.*;
 
import java.math.BigDecimal;
 
/**
 * Pago individual dentro de una venta.
 *
 * Una venta puede tener múltiples pagos simultáneos con distintos
 * métodos y monedas. Ejemplos:
 *   - 5 USD en CASH + 6.000 VES en BCV
 *   - 11 USD en BINANCE
 *   - 5 USD en CASH + resto en CREDIT (fiado)
 *
 * amount     = monto en la moneda indicada por currency
 *              (lo que el usuario ingresó / el cliente pagó realmente)
 * amount_usd = equivalente en USD al momento del pago
 *              Si currency=USD:  amount_usd = amount
 *              Si currency=VES:  amount_usd = amount / rate_bcv
 *              Se usa para reportes y balance del cliente en crédito.
 *
 * Nota: por el redondeo de bolívares, amount_usd puede no sumar
 * exactamente el total de la venta — esto es esperado y aceptado.
 */
@Entity
@Table(name = "sale_payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalePaymentEntity {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_payment")
    private Integer idPayment;
 
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_sale", nullable = false)
    private SaleEntity sale;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentType method;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 5)
    private Currency currency;
 
    // Monto en la moneda original del pago
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
 
    // Equivalente en USD — calculado en SalesService al momento del registro
    @Column(name = "amount_usd", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountUsd;
}