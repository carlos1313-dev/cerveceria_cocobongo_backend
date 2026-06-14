# Configuración de Variables de Entorno

## Descripción

Este proyecto utiliza variables de entorno para gestionar datos sensibles como contraseñas, claves JWT, y configuraciones de base de datos. **Nunca deben comprometerse credenciales reales en el repositorio**.

## Configuración Rápida (Desarrollo)

### 1. Copiar plantilla de variables

```bash
cp .env.example .env
```

### 2. Editar `.env` con tus valores locales

```
DATABASE_URL=jdbc:postgresql://localhost:5432/cocobongo_db
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=tu_password_local
JWT_SECRET=tu_jwt_secret_desarrollo
ADMIN_PASSWORD=admin123
PORT=8082
```

### 3. La app cargará automáticamente

La clase `EnvConfig` carga automáticamente las variables del `.env` al iniciar la aplicación.

## Variables de Entorno Disponibles

| Variable | Propósito | Dev Default | Prod Requerido |
|----------|-----------|-------------|----------------|
| `DATABASE_URL` | URL de conexión PostgreSQL | ✓ | ✓ |
| `DATABASE_USERNAME` | Usuario de BD | ✓ | ✓ |
| `DATABASE_PASSWORD` | Contraseña de BD | ✓ | ✓ |
| `JWT_SECRET` | Clave secreta para JWT | ✓ (débil) | ✓ |
| `ADMIN_PASSWORD` | Contraseña admin inicial | ✓ | - |
| `PORT` | Puerto del servidor | `8082` | - |

## Seguridad

### .env está en .gitignore

```
.env
.env.local
.env.*.local
```

**⚠️ IMPORTANTE:** El archivo `.env` no se commitea nunca. Solo se commitea `.env.example`.

### Para Producción

1. **Nunca** uses `.env` en producción
2. Setea las variables directamente en el servidor:
   - Docker: `docker run -e DATABASE_URL="..." -e JWT_SECRET="..."`
   - Heroku: `heroku config:set DATABASE_URL="..."`
   - Kubernetes: Usa Secrets
   - VM/Server: Variables de entorno del SO

3. Genera un JWT_SECRET fuerte:

```bash
openssl rand -base64 32
```

## Mapeo en application.yaml

### Dev (con valores por defecto)
```yaml
app:
  jwt:
    secret: ${JWT_SECRET:dev_secret_key_cambiar_en_produccion_minimo_256_bits}
```

### Prod (sin valores por defecto)
```yaml
app:
  jwt:
    secret: ${JWT_SECRET}  # Forzar que se setee
```

## Troubleshooting

### Error: "Could not resolve placeholder"

**Causa:** Variable no está definida en `.env` ni en variables del sistema.

**Solución:** 
```bash
# Verificar que .env existe y tiene la variable
cat .env | grep JWT_SECRET

# O setear manualmente
export JWT_SECRET="tu_secret"
```

### Error de conexión a BD

**Causa:** Las variables de BD no se cargaron.

**Solución:**
```bash
# Verificar conexión manual
psql -h localhost -U postgres -d cocobongo_db
```

## Para el Equipo

1. **Nuevo desarrollador:** Copia `.env.example` a `.env` y edita localmente
2. **No** commitear cambios de variables sensibles
3. **Siempre** usar `.env.example` para actualizar la plantilla
4. **En deploy:** Setea variables en el entorno de la plataforma
