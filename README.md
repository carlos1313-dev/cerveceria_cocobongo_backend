# 🍺 Sistema de Gestión — Cervecería Cocobongo

Sistema web para la administración operativa y financiera de una cervecería, que automatiza el control de inventario, registro de ventas, gestión de clientes a crédito y reportes del negocio. Está orientado a dos tipos de usuarios: el **Administrador** (dueño del negocio, acceso total) y el **Empleado** (acceso limitado al registro de ventas).

---

## 👥 Integrantes

| Nombre | Código |
|---|---|
| Carlos Enrique Sangronis Ricardo | 20241020040 |
| Gabriela Martínez Silva | 20231020205 |
| Jhon Jairo O'meara Muñoz | 20231020220 |
| Juan David Diaz Páez | 20241020033 |
| Geraldine Alejandra Vargas Moreno | 20241020105 |

**Curso:** Bases de Datos · **Docente:** René Alejandro Lobo Quintero  
**Facultad de Ingeniería — Ingeniería de Sistemas · UDFJC · 2026**

---

## ✅ Requisitos Previos

Antes de ejecutar el proyecto asegúrate de tener instalado:

- [Java 17](https://adoptium.net/)
- [PostgreSQL 15+](https://www.postgresql.org/download/)
- [Maven](https://maven.apache.org/) (o usar el wrapper `mvnw` incluido)
- IDE: [VS Code](https://code.visualstudio.com/) con extensión Spring Boot, o [IntelliJ IDEA](https://www.jetbrains.com/idea/)

---

## 🚀 Instalación y Ejecución

### 1. Clonar el repositorio

```bash
git clone https://github.com/N0dadux/ProyectoFinalDBBack.git
cd ProyectoFinalDBBack
```

### 2. Cargar el esquema de base de datos

Conéctate a PostgreSQL y ejecuta el script SQL del proyecto:

```bash
psql -U postgres -c "CREATE DATABASE cocobongo_db;"
psql -U postgres -d cocobongo_db -f DDL_Cerveceria.sql
```

### 3. Configurar `application.properties`

Edita el archivo `src/main/resources/application.properties` con tus credenciales:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/cocobongo_db
spring.datasource.username=postgres
spring.datasource.password=tu_contraseña

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
```

### 4. Ejecutar la aplicación

**Entra a la carpeta del proyecto Spring Boot antes de ejecutar:**
```bash
cd cerveceria
```

**En Windows:**
```bash
mvnw.cmd spring-boot:run
```

**En Linux / macOS:**
```bash
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8082`.

---

## 🗂️ Diagrama Entidad-Relación

<img width="907" height="888" alt="Diagrama" src="https://github.com/user-attachments/assets/b3fb36f6-b3e7-4af3-a2a9-8174b95388e1" />

El modelo incluye 13 tablas principales:
`PROVIDER`, `BRANCH`, `PRODUCT`, `USERS`, `CLIENTS`, `SALE`, `SALE_DETAIL`, `INSTALLMENT`, `INVENTORY`, `INVENTORY_MOVEMENT`, `OUTGOING`, `SESSIONS`, `AUDIT`.

---

## 🌐 Endpoints de la API

### 🔐 Autenticación (`/api/v1/auth`)

| Método | URL | Descripción |
|---|---|---|
| POST | `/api/v1/auth/register` | Registrar empleado |
| POST | `/api/v1/auth/confirm-email?token=` | Confirmar correo electrónico |
| POST | `/api/v1/auth/login` | Iniciar sesión |
| POST | `/api/v1/auth/logout` | Cerrar sesión |
| POST | `/api/v1/auth/password/recover` | Recuperación de contraseña |
| POST | `/api/v1/auth/password/reset` | Reestablecer contraseña |
| POST | `/api/v1/auth/password/change` | Cambiar contraseña |
| GET | `/api/v1/auth/sessions/current` | Gestión de sesiones |
| GET | `/api/v1/auth/sessions/audit-logs` | Auditoría de accesos |
| GET | `/api/v1/auth/me` | Info del usuario autenticado y permisos |

### 📦 Inventario y Productos (`/api/v1/inventory`)

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/v1/inventory/products` | Consultar productos (filtros: branchId, search, page, size) |
| GET | `/api/v1/inventory/products/{id}` | Consultar producto por ID |
| POST | `/api/v1/inventory/products` | Crear producto |
| PUT | `/api/v1/inventory/products/{id}` | Actualizar producto |
| DELETE | `/api/v1/inventory/products/{id}` | Eliminar producto |
| GET | `/api/v1/inventory/products/alerts` | Alertas de stock bajo (filtro: branchId) |
| GET | `/api/v1/inventory` | Obtener inventario (filtros: branchId, page, size) |
| GET | `/api/v1/inventory/{productId}` | Obtener stock de un producto |
| POST | `/api/v1/inventory/entries` | Registro manual de entrada a inventario |
| POST | `/api/v1/inventory/adjustments` | Ajuste manual de stock |
| GET | `/api/v1/inventory/movements` | Historial de movimientos (filtros: productId, branchId, type, reason, from, to) |

### 🏭 Proveedores (`/api/v1/inventory/providers`)

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/v1/inventory/providers` | Listar proveedores activos |
| POST | `/api/v1/inventory/providers` | Crear proveedor |
| PUT | `/api/v1/inventory/providers/{id}` | Actualizar proveedor |
| DELETE | `/api/v1/inventory/providers/{id}` | Eliminar proveedor |

### 🧾 Ventas (`/api/v1/sales`)

| Método | URL | Descripción |
|---|---|---|
| POST | `/api/v1/sales/branch/{id}` | Registrar venta |
| GET | `/api/v1/sales/{id}` | Consultar venta por ID |
| GET | `/api/v1/sales` | Listar ventas (filtros: branchId, from, to, page, size) |
| GET | `/api/v1/sales/client/{clientId}` | Consultar ventas de un cliente |

### 👤 Clientes (`/api/v1/clients`)

| Método | URL | Descripción |
|---|---|---|
| POST | `/api/v1/clients` | Registrar cliente |
| GET | `/api/v1/clients/{id}` | Consultar cliente |
| PUT | `/api/v1/clients/{id}` | Actualizar cliente |
| DELETE | `/api/v1/clients/{id}` | Eliminar cliente |
| GET | `/api/v1/clients` | Listar clientes con saldo pendiente |
| GET | `/api/v1/clients/balances` | Consultar saldos pendientes |
| PUT | `/api/v1/clients/{id}/installments` | Registrar abono de un cliente |
| GET | `/api/v1/clients/{id}/account-status` | Historial de ventas a crédito y abonos |
| GET | `/api/v1/clients/{id}/pending-sales` | Consultar ventas pendientes del cliente |

### 📊 Reportes (`/api/v1/reports`)

| Método | URL | Descripción |
|---|---|---|
| GET | `/api/v1/reports/sales` | Ventas por periodo (filtros: from, to, branchId) |
| GET | `/api/v1/reports/summary` | Resumen del negocio / métricas (filtro: period) |
| GET | `/api/v1/reports/profit-by-branch` | Ventas y ganancias por sucursal |
| GET | `/api/v1/reports/period-summary` | Resumen periodo |

### 💸 Gastos (`/api/v1/outgoings`)

| Método | URL | Descripción |
|---|---|---|
| POST | `/api/v1/outgoings/register` | Registrar gasto |
| GET | `/api/v1/outgoings` | Listar gastos |
| GET | `/api/v1/outgoings/{id}` | Consultar gasto por ID |
| PUT | `/api/v1/outgoings/{id}` | Actualizar gasto |
| GET | `/api/v1/outgoings/balance` | Balance del negocio (ingresos − gastos) |

### 🏢 Sucursales (`/api/v1/branches`)

| Método | URL | Descripción |
|---|---|---|
| POST | `/api/v1/branches` | Crear sucursal |
| GET | `/api/v1/branches` | Listar sucursales |
| GET | `/api/v1/branches/{id}` | Consultar sucursal por ID |
| PUT | `/api/v1/branches/{id}` | Actualizar sucursal |
| DELETE | `/api/v1/branches/{id}` | Eliminar sucursal |

### 👥 Usuarios (`/api/v1/users`)

| Método | URL | Descripción |
|---|---|---|
| POST | `/api/v1/users` | Crear usuario |
| GET | `/api/v1/users` | Listar usuarios |
| GET | `/api/v1/users/{userId}` | Consultar usuario por ID |
| PUT | `/api/v1/users/{userId}` | Modificar usuario |
| PATCH | `/api/v1/users/{id}/deactivate` | Desactivar usuario |
| PATCH | `/api/v1/users/{id}/activate` | Activar usuario |
| PATCH | `/api/v1/users/{id}/role` | Cambiar rol de usuario |
| GET | `/api/v1/users?userId=&action=&from=&to=` | Consultar auditoría de usuarios |

---

## ⚙️ Requerimientos Funcionales Implementados

| Código | Módulo | Funcionalidad |
|---|---|---|
| RF-INV-01 a RF-INV-06 | **Inventario** | CRUD de productos, registro de entradas, descuento automático de stock al vender, consulta por sucursal y alerta de stock mínimo. |
| RF-VEN-01 a RF-VEN-06 | **Ventas** | Registro de ventas con cálculo automático del total, selección de método de pago (efectivo, tarjeta, transferencia, crédito), asociación a sucursal y usuario. |
| RF-CLI-01 a RF-CLI-08 | **Clientes** | CRUD de clientes, gestión de saldo pendiente, registro de abonos parciales/totales, historial de cuenta y alerta visual por deuda al registrar nuevas ventas. |
| RF-REP-01 a RF-REP-03 | **Reportes** | Consulta de ventas por día/semana/mes/sucursal, estadísticas de ingresos totales y productos más vendidos, cálculo de ganancia estimada. |
| RF-SUC-01 | **Sucursales** | Creación, edición, consulta y eliminación de sucursales del negocio. |
| RF-GAS-01 a RF-GAS-03 | **Gastos** | Registro de egresos (arriendo, mantenimiento, servicios, etc.) y cálculo del balance general (ingresos − gastos). |
| RF-SEG-01 a RF-SEG-10 | **Seguridad** | Autenticación por token, roles ADMIN/EMPLOYEE, recuperación y cambio de contraseña, auditoría de accesos, gestión de sesiones. |
