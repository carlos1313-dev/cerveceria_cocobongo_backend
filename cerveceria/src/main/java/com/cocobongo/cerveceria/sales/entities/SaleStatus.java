package com.cocobongo.cerveceria.sales.entities;

/**
 * Estado del ciclo de vida de una venta.
 *
 * COMPLETED → venta pagada en su totalidad (CASH, CARD, TRANSFER).
 * PENDING   → venta a crédito con saldo pendiente de cobro.
 * CANCELLED → venta anulada; genera movimiento RETURN en inventory_movement
 *             para restaurar el stock descontado.
 */
public enum SaleStatus {
    COMPLETED,
    PENDING,
    CANCELLED
}
 