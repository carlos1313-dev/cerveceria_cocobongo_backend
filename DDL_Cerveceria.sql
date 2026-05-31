-- =============================================================================
-- SISTEMA DE GESTIÓN CERVECERÍA
-- DDL PostgreSQL — Versión corregida y revisada
-- =============================================================================
-- Convenciones:
--   - snake_case para tablas y columnas
--   - SERIAL para PKs simples (autoincremento)
--   - PKs compuestas donde corresponde (inventory)
--   - Soft delete via is_active (no se borra data, se desactiva)
--   - Campos calculados (subtotal, balance) gestionados desde la aplicación
--   - quantity  = unidades físicas (productos)
--   - total     = valor monetario (ventas, gastos)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- LIMPIEZA (descomentar solo en entorno de desarrollo)
-- -----------------------------------------------------------------------------
-- DROP TABLE IF EXISTS audit                CASCADE;
-- DROP TABLE IF EXISTS sessions             CASCADE;
-- DROP TABLE IF EXISTS installment          CASCADE;
-- DROP TABLE IF EXISTS sale_detail          CASCADE;
-- DROP TABLE IF EXISTS inventory_movement   CASCADE;
-- DROP TABLE IF EXISTS inventory            CASCADE;
-- DROP TABLE IF EXISTS sale                 CASCADE;
-- DROP TABLE IF EXISTS outgoing             CASCADE;
-- DROP TABLE IF EXISTS client               CASCADE;
-- DROP TABLE IF EXISTS product              CASCADE;
-- DROP TABLE IF EXISTS users                CASCADE;
-- DROP TABLE IF EXISTS provider             CASCADE;
-- DROP TABLE IF EXISTS branch               CASCADE;


-- =============================================================================
-- ENTIDADES BASE
-- =============================================================================

CREATE TABLE provider (
    id_provider  SERIAL       PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    telephone    VARCHAR(20),
    address      TEXT,
    email        VARCHAR(100),
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_provider_email
        CHECK (email IS NULL
               OR email ~* '^[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_provider_telephone
        CHECK (telephone IS NULL
               OR telephone ~ '^[0-9\s\+\-\(\)]{7,20}$')
);

-- -----------------------------------------------------------------------------

CREATE TABLE branch (
    id_branch  SERIAL       PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    address    TEXT,
    city       VARCHAR(50),
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE
);

-- -----------------------------------------------------------------------------

CREATE TABLE product (
    id_product   SERIAL        PRIMARY KEY,
    id_provider  INT,                          -- nullable: productos MADE no tienen proveedor
    name         VARCHAR(100)  NOT NULL,
    description  TEXT,
    type         VARCHAR(10)   NOT NULL DEFAULT 'RESALE',
        -- RESALE  = producto comprado para revender directamente
        -- SUPPLY  = insumo usado para elaborar otro producto
        -- MADE    = producto elaborado a partir de insumos propios
    cost         NUMERIC(10,2) NOT NULL DEFAULT 0,  -- precio de compra / costo de producción
    price        NUMERIC(10,2) NOT NULL DEFAULT 0,  -- precio de venta al público
    is_active    BOOLEAN       NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_product_type
        CHECK (type IN ('RESALE', 'SUPPLY', 'MADE')),
    CONSTRAINT chk_provider_required
        CHECK (type = 'MADE' OR id_provider IS NOT NULL),
    CONSTRAINT chk_product_cost_positive
        CHECK (cost >= 0),
    CONSTRAINT chk_product_price_positive
        CHECK (price >= 0),

    CONSTRAINT fk_product_provider
        FOREIGN KEY (id_provider)
        REFERENCES provider(id_provider)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- -----------------------------------------------------------------------------

CREATE TABLE users (
    id_user             SERIAL        PRIMARY KEY,
    id_branch           INT,                       -- sucursal a la que pertenece el usuario
    name                VARCHAR(100)  NOT NULL,
    email               VARCHAR(100)  NOT NULL UNIQUE,
    password_hash       VARCHAR(255)  NOT NULL,
    role                VARCHAR(20)   NOT NULL DEFAULT 'EMPLOYEE',
    is_active           BOOLEAN       NOT NULL DEFAULT TRUE,
    password_changed_at TIMESTAMP,
    created_at          TIMESTAMP     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_users_role
        CHECK (role IN ('ADMIN', 'EMPLOYEE')),
    CONSTRAINT chk_users_email
        CHECK (email ~* '^[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_users_password_length
        CHECK (LENGTH(password_hash) >= 60),

    CONSTRAINT fk_users_branch
        FOREIGN KEY (id_branch)
        REFERENCES branch(id_branch)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

-- -----------------------------------------------------------------------------

CREATE TABLE client (
    id_client  SERIAL        PRIMARY KEY,
    name       VARCHAR(100)  NOT NULL,
    telephone  VARCHAR(20),
    email      VARCHAR(100),
    -- CAMPO CALCULADO: no actualizar con UPDATE directo.
    -- balance = SUM(sale.total WHERE payment_type='CREDIT') - SUM(installment.amount)
    -- Actualizar siempre desde la lógica de aplicación al registrar ventas o abonos.
    balance    NUMERIC(10,2) NOT NULL DEFAULT 0,
    is_active  BOOLEAN       NOT NULL DEFAULT TRUE,

    CONSTRAINT chk_client_balance
        CHECK (balance >= 0),
    CONSTRAINT chk_client_email
        CHECK (email IS NULL
               OR email ~* '^[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT chk_client_telephone
        CHECK (telephone IS NULL
               OR telephone ~ '^[0-9\s\+\-\(\)]{7,20}$')
);


-- =============================================================================
-- VENTAS
-- =============================================================================

CREATE TABLE sale (
    id_sale      SERIAL        PRIMARY KEY,
    id_branch    INT           NOT NULL,
    id_user      INT           NOT NULL,
    id_client    INT,                          -- nullable: ventas rápidas sin cliente registrado
    sale_date    TIMESTAMP     NOT NULL DEFAULT NOW(),
    total        NUMERIC(10,2) NOT NULL DEFAULT 0,
    payment_type VARCHAR(20)   NOT NULL,
        -- CASH     = pago en efectivo
        -- CARD     = pago con tarjeta
        -- TRANSFER = pago por transferencia
        -- CREDIT   = venta a crédito (fiado)
    status       VARCHAR(15)   NOT NULL DEFAULT 'COMPLETED',
        -- COMPLETED = pagada en su totalidad
        -- PENDING   = venta a crédito con saldo pendiente
        -- CANCELLED = venta anulada (genera movimiento RETURN en inventory_movement)

    CONSTRAINT chk_sale_payment_type
        CHECK (payment_type IN ('CASH', 'CARD', 'TRANSFER', 'CREDIT')),
    CONSTRAINT chk_sale_status
        CHECK (status IN ('COMPLETED', 'CANCELLED', 'PENDING')),
    CONSTRAINT chk_sale_date_not_future
        CHECK (sale_date <= CURRENT_DATE),
    CONSTRAINT chk_sale_total_positive
        CHECK (total >= 0),
    CONSTRAINT chk_credit_requires_client
        CHECK (payment_type != 'CREDIT' OR id_client IS NOT NULL),

    CONSTRAINT fk_sale_branch
        FOREIGN KEY (id_branch)
        REFERENCES branch(id_branch)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_sale_user
        FOREIGN KEY (id_user)
        REFERENCES users(id_user)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_sale_client
        FOREIGN KEY (id_client)
        REFERENCES client(id_client)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

-- -----------------------------------------------------------------------------

CREATE TABLE sale_detail (
    id_sale_detail  SERIAL        PRIMARY KEY,
    id_sale         INT           NOT NULL,
    id_product      INT           NOT NULL,
    quantity        INT           NOT NULL,    -- unidades vendidas
    unit_price      NUMERIC(10,2) NOT NULL,   -- precio vigente al momento de la venta (snapshot)
    subtotal        NUMERIC(10,2) NOT NULL,   -- CALCULADO: quantity * unit_price — validar en app

    CONSTRAINT chk_sale_detail_quantity
        CHECK (quantity > 0),
    CONSTRAINT chk_sale_detail_unit_price
        CHECK (unit_price >= 0),
    CONSTRAINT chk_sale_detail_subtotal
        CHECK (subtotal >= 0),
    CONSTRAINT chk_sale_detail_subtotal_math
        CHECK (ABS(subtotal - (quantity * unit_price)) <= 0.01),

    CONSTRAINT fk_sale_detail_sale
        FOREIGN KEY (id_sale)
        REFERENCES sale(id_sale)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_sale_detail_product
        FOREIGN KEY (id_product)
        REFERENCES product(id_product)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- -----------------------------------------------------------------------------

CREATE TABLE installment (
    id_installment  SERIAL        PRIMARY KEY,
    id_client       INT           NOT NULL,
    id_user         INT           NOT NULL,  -- usuario que registró el abono
    id_sale         INT,                     -- nullable: abono puede ser general (no ligado a una venta)
    amount          NUMERIC(10,2) NOT NULL,
    payment_date    TIMESTAMP     NOT NULL DEFAULT NOW(),
    notes           TEXT,                    -- observaciones opcionales del abono

    CONSTRAINT chk_installment_amount
        CHECK (amount > 0),

    CONSTRAINT fk_installment_client
        FOREIGN KEY (id_client)
        REFERENCES client(id_client)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_installment_user
        FOREIGN KEY (id_user)
        REFERENCES users(id_user)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_installment_sale
        FOREIGN KEY (id_sale)
        REFERENCES sale(id_sale)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);


-- =============================================================================
-- INVENTARIO
-- =============================================================================

CREATE TABLE inventory (
    id_product  INT  NOT NULL,
    id_branch   INT  NOT NULL,
    stock       INT  NOT NULL DEFAULT 0,
    min_stock   INT  NOT NULL DEFAULT 0,

    PRIMARY KEY (id_product, id_branch),

    CONSTRAINT chk_inventory_stock
        CHECK (stock >= 0),
    CONSTRAINT chk_inventory_min_stock
        CHECK (min_stock >= 0),

    CONSTRAINT fk_inventory_product
        FOREIGN KEY (id_product)
        REFERENCES product(id_product)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_inventory_branch
        FOREIGN KEY (id_branch)
        REFERENCES branch(id_branch)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- -----------------------------------------------------------------------------

CREATE TABLE inventory_movement (
    id_movement   SERIAL      PRIMARY KEY,
    id_product    INT         NOT NULL,
    id_branch     INT         NOT NULL,
    id_user       INT         NOT NULL,
    type          VARCHAR(10) NOT NULL,
        -- IN  = entrada de stock
        -- OUT = salida de stock
    reason        VARCHAR(15) NOT NULL,
        -- PURCHASE   = entrada por compra a proveedor
        -- SALE       = salida por venta (se crea automáticamente al registrar una venta)
        -- PRODUCTION = entrada (producto elaborado) o salida (insumo consumido)
        -- TRANSFER   = traslado entre sucursales (genera 2 movimientos vinculados: OUT + IN)
        -- ADJUSTMENT = corrección manual de inventario
        -- RETURN     = devolución por venta anulada (restaura stock)
    quantity      INT         NOT NULL,
    movement_date TIMESTAMP   NOT NULL DEFAULT NOW(),
    id_reference  INT,
        -- referencia flexible según el motivo:
        --   SALE / RETURN  → id_sale
        --   TRANSFER       → id del movimiento par (OUT↔IN se referencian mutuamente)
        --   PURCHASE       → NULL (o futuro id de purchase_order si se implementa)
        --   PRODUCTION     → NULL (o futuro id de production_order)

    CONSTRAINT chk_inv_mov_type
        CHECK (type IN ('IN', 'OUT')),
    CONSTRAINT chk_inv_mov_reason
        CHECK (reason IN ('PURCHASE', 'SALE', 'PRODUCTION', 'TRANSFER', 'ADJUSTMENT', 'RETURN')),
    CONSTRAINT chk_inv_mov_quantity
        CHECK (quantity > 0),
    CONSTRAINT chk_inv_mov_type_reason
        CHECK (
            (reason = 'PURCHASE'  AND type = 'IN' )
         OR (reason = 'SALE'      AND type = 'OUT')
         OR (reason = 'RETURN'    AND type = 'IN' )
         OR  reason IN ('PRODUCTION', 'TRANSFER', 'ADJUSTMENT')
        ),
    CONSTRAINT chk_inv_mov_reference_required
        CHECK (
            reason NOT IN ('SALE', 'RETURN', 'TRANSFER')
            OR id_reference IS NOT NULL
        ),

    CONSTRAINT fk_inv_mov_product
        FOREIGN KEY (id_product)
        REFERENCES product(id_product)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_inv_mov_branch
        FOREIGN KEY (id_branch)
        REFERENCES branch(id_branch)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_inv_mov_user
        FOREIGN KEY (id_user)
        REFERENCES users(id_user)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);


-- =============================================================================
-- GASTOS
-- =============================================================================

CREATE TABLE outgoing (
    id_outgoing  SERIAL        PRIMARY KEY,
    id_branch    INT           NOT NULL,
    id_user      INT           NOT NULL,
    type         VARCHAR(20)   NOT NULL,
        -- PERSONAL     = gastos personales del dueño
        -- MAINTENANCE  = mantenimiento del local o equipos
        -- RENT         = arriendo del local
        -- SERVICES     = servicios (internet, agua, luz, etc.)
        -- EMPLOYEE     = pago de empleados
        -- OTHER        = cualquier gasto no categorizado
    date         DATE          NOT NULL DEFAULT CURRENT_DATE,
    total        NUMERIC(10,2) NOT NULL,
    description  TEXT,

    CONSTRAINT chk_outgoing_type
        CHECK (type IN ('PERSONAL', 'MAINTENANCE', 'RENT', 'SERVICES', 'EMPLOYEE', 'OTHER')),
    CONSTRAINT chk_outgoing_total
        CHECK (total > 0),
    CONSTRAINT chk_outgoing_date_not_future
        CHECK (date <= CURRENT_DATE),

    CONSTRAINT fk_outgoing_branch
        FOREIGN KEY (id_branch)
        REFERENCES branch(id_branch)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_outgoing_user
        FOREIGN KEY (id_user)
        REFERENCES users(id_user)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);


-- =============================================================================
-- SEGURIDAD Y AUDITORÍA
-- =============================================================================

CREATE TABLE sessions (
    id_session  SERIAL        PRIMARY KEY,
    id_user     INT           NOT NULL,
    token       VARCHAR(512)  NOT NULL UNIQUE,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    expires_at  TIMESTAMP     NOT NULL,
    is_active   BOOLEAN       NOT NULL DEFAULT TRUE,
    revoked_at  TIMESTAMP,    -- NULL mientras la sesión esté activa
                              -- se llena al hacer logout, expirar o revocar por admin

    CONSTRAINT chk_sessions_expiry
        CHECK (expires_at > created_at),

    CONSTRAINT fk_sessions_user
        FOREIGN KEY (id_user)
        REFERENCES users(id_user)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);

-- -----------------------------------------------------------------------------

-- Catálogo de acciones esperadas en audit.action:
--   Autenticación : LOGIN_OK, LOGIN_FAIL, LOGOUT, PASSWORD_CHANGED
--   Usuarios      : USER_CREATED, USER_UPDATED, USER_DEACTIVATED
--   Ventas        : SALE_CREATED, SALE_CANCELLED
--   Inventario    : PRODUCT_CREATED, PRODUCT_UPDATED, PRODUCT_DEACTIVATED,
--                   INVENTORY_ADJUSTED
--   Gastos        : OUTGOING_CREATED
--   Clientes      : CLIENT_CREATED, CLIENT_UPDATED, INSTALLMENT_REGISTERED
--   Sucursales    : BRANCH_CREATED, BRANCH_UPDATED

CREATE TABLE audit (
    id_audit    SERIAL      PRIMARY KEY,
    id_user     INT,                       -- nullable: LOGIN_FAIL puede no tener usuario autenticado
    id_session  INT,                       -- permite cruzar acción con sesión específica
    action      VARCHAR(50) NOT NULL,      -- ver catálogo arriba
    table_name  VARCHAR(50),               -- tabla afectada (ej: 'sale', 'product')
    id_record   INT,                       -- PK del registro afectado (no es FK, apunta a tablas distintas)
    detail      TEXT,                      -- descripción adicional o payload JSON si se necesita
    ip_address  VARCHAR(45),               -- soporta IPv4 e IPv6
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_audit_user
        FOREIGN KEY (id_user)
        REFERENCES users(id_user)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CONSTRAINT fk_audit_session
        FOREIGN KEY (id_session)
        REFERENCES sessions(id_session)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);


-- =============================================================================
-- ÍNDICES
-- =============================================================================

-- Ventas: consultas por fecha y sucursal (reportes diarios/semanales/mensuales)
CREATE INDEX idx_sale_date         ON sale(sale_date);
CREATE INDEX idx_sale_branch       ON sale(id_branch);
CREATE INDEX idx_sale_client       ON sale(id_client);

-- Detalle de ventas: joins frecuentes desde sale
CREATE INDEX idx_sale_detail_sale  ON sale_detail(id_sale);

-- Inventario: consultas de stock por producto y sucursal
CREATE INDEX idx_inv_mov_product   ON inventory_movement(id_product);
CREATE INDEX idx_inv_mov_branch    ON inventory_movement(id_branch);
CREATE INDEX idx_inv_mov_date      ON inventory_movement(movement_date);

-- Clientes: consulta de saldo y abonos
CREATE INDEX idx_installment_client ON installment(id_client);
CREATE INDEX idx_installment_sale   ON installment(id_sale);

-- Gastos: reportes por fecha y sucursal
CREATE INDEX idx_outgoing_branch    ON outgoing(id_branch);
CREATE INDEX idx_outgoing_date      ON outgoing(date);

-- Sesiones: se consulta en cada request autenticado
CREATE INDEX idx_sessions_token     ON sessions(token);
CREATE INDEX idx_sessions_user      ON sessions(id_user);

-- Auditoría: consultas por usuario, acción y fecha
CREATE INDEX idx_audit_user         ON audit(id_user);
CREATE INDEX idx_audit_action       ON audit(action);
CREATE INDEX idx_audit_date         ON audit(created_at);

-- =============================================================================
-- VISTAS
-- =============================================================================

-- Vista: reporte diario (ventas de hoy)
CREATE VIEW v_daily_report AS
SELECT
    s.id_sale,
    s.sale_date,
    s.total,
    s.status,
    s.id_branch,
    s.id_client,
    s.id_user
FROM sale s
WHERE DATE(s.sale_date) = CURRENT_DATE
    AND s.status = 'COMPLETED';

-- Vista: reporte semanal (últimos 7 días)
CREATE VIEW v_weekly_report AS
SELECT
    s.id_sale,
    s.sale_date,
    s.total,
    s.status,
    s.id_branch,
    s.id_client,
    s.id_user
FROM sale s
WHERE s.sale_date >= CURRENT_DATE - INTERVAL 7 DAY
    AND s.sale_date <  CURRENT_DATE + INTERVAL 1 DAY
     AND s.status = 'COMPLETED';

-- Vista: reporte mensual (últimos 30 días)
CREATE VIEW v_monthly_report AS
SELECT
    s.id_sale,
    s.sale_date,
    s.total,
    s.status,
    s.id_branch,
    s.id_client,
    s.id_user
FROM sale s
WHERE s.sale_date >= CURRENT_DATE - INTERVAL 30 DAY
    AND s.sale_date <  CURRENT_DATE + INTERVAL 1 DAY
    AND s.status = 'COMPLETED';


-- Vista integrada: resumen agregado por período y sucursal
CREATE VIEW v_period_summary AS
SELECT
    s.id_branch,
    b.name                              AS branch_name,
    DATE(s.sale_date)                   AS sale_day,
    COUNT(DISTINCT s.id_sale)           AS total_sales,
    SUM(s.total)                        AS gross_income,
    SUM((sd.unit_price - p.cost) * sd.quantity)                 AS estimated_profit
FROM sale s
JOIN sale_detail sd ON sd.id_sale   = s.id_sale
JOIN product     p  ON p.id_product = sd.id_product
JOIN branch      b  ON b.id_branch  = s.id_branch
WHERE s.status = 'COMPLETED'
GROUP BY s.id_branch, b.name, DATE(s.sale_date);