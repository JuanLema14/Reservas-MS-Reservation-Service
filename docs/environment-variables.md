# Variables de Entorno - Reservas-MS-Reservation-Service

Este documento describe todas las variables de entorno necesarias para el despliegue del microservicio de Gestión de Reservas.

## Archivo de Configuración

Copia el archivo `.env.example` a `.env` y configura los valores:

```bash
cp .env.example .env
```

## Variables Requeridas

### 1. Perfil de Spring

| Variable | Descripción | Valor por Defecto | Opciones |
|----------|-------------|-------------------|----------|
| `SPRING_PROFILE` | Perfil activo de Spring Boot | `dev` | `dev`, `test`, `prod` |

### 2. Configuración de Base de Datos (Supabase)

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `DB_URL` | URL JDBC de PostgreSQL (Transaction Pooler IPv4) | `jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0` |
| `DB_USER` | Usuario de la base de datos | `postgres.[PROJECT-REF]` |
| `DB_PASSWORD` | Contraseña de Supabase | `[TU-CONTRASEÑA]` |

**Nota:** Se recomienda usar el Transaction Pooler de Supabase (puerto 6543) para compatibilidad con IPv4.

### 3. Configuración JWT

| Variable | Descripción | Recomendación |
|----------|-------------|---------------|
| `JWT_SECRET` | Secreto para firmar/validar tokens JWT | Generar con: `openssl rand -base64 64` |
| `JWT_ACCESS_EXPIRATION` | Tiempo de expiración del access token en milisegundos | `900000` (15 minutos) |
| `JWT_REFRESH_EXPIRATION` | Tiempo de expiración del refresh token en milisegundos | `604800000` (7 días) |

⚠️ **IMPORTANTE:** El `JWT_SECRET` debe ser el **MISMO VALOR** en todos los microservicios para que la validación de tokens funcione correctamente.

### 4. URL del Frontend

| Variable | Descripción | Valor Local | Valor Producción |
|----------|-------------|-------------|------------------|
| `FRONTEND_URL` | URL base para enlaces de confirmación/cancelación | `http://localhost:3000` | `https://tu-dominio.com` |

### 5. Configuración de Email

El Reservation Service envía emails de confirmación y cancelación de reservas:

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `EMAIL_USERNAME` | Dirección de correo Gmail | `plataformareservas.codefactory@gmail.com` |
| `EMAIL_PASSWORD` | App Password de Google (16 caracteres) | `xxxx xxxx xxxx xxxx` |
| `EMAIL_ENABLED` | Habilitar envío de emails | `true` |

**Cómo generar App Password en Gmail:**
1. Ve a tu cuenta de Google → Seguridad
2. Busca "Contraseñas de aplicaciones" (App Passwords)
3. Genera una nueva contraseña para "Correo"
4. Copia la contraseña de 16 caracteres

### 6. URLs de Servicios Externos

| Variable | Descripción | Puerto Local | Producción |
|----------|-------------|--------------|------------|
| `SERVICES_AUTH_URL` | URL del Auth Service para validación JWT | `http://localhost:8081` | `https://ms-auth-service.onrender.com` |
| `SERVICES_CATALOG_URL` | URL del Catalog Service para información de servicios | `http://localhost:8082` | `https://ms-catalog-service.onrender.com` |
| `SERVICES_SCHEDULE_URL` | URL del Schedule Service para validación de empleados y horarios | `http://localhost:8083` | `https://ms-schedule-service.onrender.com` |

## Ejemplo Completo (.env)

```bash
# ======================
# SPRING PROFILE
# ======================
SPRING_PROFILE=dev

# ======================
# DATABASE CONFIG
# ======================
DB_URL=jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0
DB_USER=postgres.[TU-PROJECT-REF]
DB_PASSWORD=[TU-CONTRASEÑA-DE-SUPABASE]

# ======================
# JWT CONFIG (MISMO EN TODOS LOS MS)
# ======================
JWT_SECRET=[TU-JWT-SECRET-SEGURA]
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

# ======================
# EMAIL CONFIG
# ======================
EMAIL_USERNAME=plataformareservas.codefactory@gmail.com
EMAIL_PASSWORD=[TU-APP-PASSWORD]
EMAIL_ENABLED=true

# ======================
# FRONTEND URL
# ======================
FRONTEND_URL=http://localhost:3000

# ======================
# EXTERNAL SERVICES URLs
# ======================
SERVICES_AUTH_URL=http://localhost:8081
SERVICES_CATALOG_URL=http://localhost:8082
SERVICES_SCHEDULE_URL=http://localhost:8083
```

## Despliegue en Producción (Render)

Cuando despliegues en Render:

1. Configura todas las variables en el dashboard de Render
2. Actualiza las URLs de servicios externos a las URLs de producción
3. Asegúrate de que el `JWT_SECRET` sea idéntico al de los otros microservicios
4. Configura `EMAIL_ENABLED=true` para activar notificaciones por email

## Verificación

Para verificar la configuración:

```bash
# Ver el perfil activo
cat .env | grep SPRING_PROFILE

# Iniciar el servicio
mvn spring-boot:run
```

En los logs verás la validación de las variables configuradas. Si el email está habilitado, también verás logs de debug del servicio de email.

## Notas Específicas del Reservation Service

- Este microservicio **depende de tres servicios externos**:
  - **Auth Service**: Validación JWT de clientes y proveedores
  - **Catalog Service**: Información de servicios (nombre, duración, precio)
  - **Schedule Service**: Validación de empleados y disponibilidad de horarios

- Si algún servicio externo no está disponible, el Reservation Service mostrará errores controlados
  - La reserva puede continuar creándose si el Schedule Service falla (graceful degradation)

- El envío de emails es **opcional** y puede deshabilitarse con `EMAIL_ENABLED=false`

- La base de datos contiene la tabla `reserva` con la estructura:
  - `id_reserva`: UUID único
  - `id_cliente`, `id_servicio`, `id_empleado`, `id_proveedor`: Referencias a otros microservicios
  - `fecha_hora_inicio`, `fecha_hora_fin`: Fecha y hora de la reserva
  - `estado`: PENDIENTE, CONFIRMADA, EN_PROGRESO, COMPLETADA, CANCELADA, NO_SHOW
  - `comentarios`: Notas opcionales del cliente