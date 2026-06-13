package com.cocobongo.cerveceria.sales.entities;
 
/**
 * Métodos de pago disponibles.
 *
 * Cambios respecto a la versión anterior:
 *   - Eliminados: CARD, TRANSFER  (absorbidos por BCV)
 *   - Añadidos:   BCV, BINANCE, BANCRECER
 *   - Conservado: CASH, CREDIT
 *
 * BCV      = pago móvil, transferencia bancaria o tarjeta de débito/crédito
 *            en el sistema bancario venezolano (equivale al antiguo CARD+TRANSFER)
 * BINANCE  = pago via app Binance (cripto/stablecoins)
 * BANCRECER = punto de venta Bancrecer
 * CASH     = efectivo (USD o VES según currency del SalePaymentEntity)
 * CREDIT   = fiado — requiere cliente asociado en SaleEntity
 */
public enum PaymentType {
    CASH,
    BCV,
    BINANCE,
    BANCRECER,
    CREDIT
}
 