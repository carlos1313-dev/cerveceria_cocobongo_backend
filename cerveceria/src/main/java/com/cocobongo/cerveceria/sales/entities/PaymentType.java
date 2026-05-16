package com.cocobongo.cerveceria.sales.entities;

/**
 * Métodos de pago disponibles para una venta.
 * Vive en el módulo sales — no justifica módulo propio porque
 * es un tipo de dato sin lógica ni repositorio independiente.
 *
 * Si en el futuro se integran pasarelas de pago (PayU, Stripe, etc.)
 * este enum sería el punto de extensión natural.
 */
public enum PaymentType {
    CASH,       // Efectivo
    CARD,       // Tarjeta
    TRANSFER,   // Transferencia bancaria
    CREDIT      // Fiado — requiere id_client obligatorio (ver SaleEntity constraint)
}