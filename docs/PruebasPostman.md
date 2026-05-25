# Pruebas de API - MS-Reservation-Service

Este documento contiene las pruebas de API para el microservicio de Gestión de Reservas.

## Configuración Inicial

### Variables de Entorno

Para ejecutar las pruebas, asegúrate de configurar las siguientes variables en tu archivo `.env`:

```bash
# SPRING PROFILE
SPRING_PROFILE=dev

# DATABASE CONFIG - SUPABASE (Transaction Pooler - IPv4 compatible)
DB_URL=jdbc:postgresql://aws-1-us-west-2.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0
DB_USER=postgres.[TU-PROJECT-REF]
DB_PASSWORD=[TU-CONTRASEÑA-DE-SUPABASE]

# EXTERNAL SERVICES URLs
SERVICES_AUTH_URL=http://localhost:8081
SERVICES_CATALOG_URL=http://localhost:8082
SERVICES_SCHEDULE_URL=http://localhost:8083
```

### Configuración de JWT

El microservicio de Reservas requiere autenticación JWT para operaciones que requieren roles específicos (CLIENTE, PROVEEDOR, ADMIN). Debes obtener un token JWT del microservicio de Autenticación.

**Para obtener un token JWT:**
1. Inicia sesión en el Auth Service: `POST http://localhost:8081/api/auth/login`
2. Usa el `accessToken` retornado en el header `Authorization: Bearer [JWT_TOKEN]`

---

## Health Check

### 1. Health Check - Success

**Nombre:** Health Check - Success
**URL:** `http://localhost:8084/api/`
**Método:** GET
**Código esperado:** 200 OK
**Response esperado:**
```json
{
    "status": "UP"
}
```

---

### 2. Version Check - Success

**Nombre:** Version Check - Success
**URL:** `http://localhost:8084/api/version`
**Método:** GET
**Código esperado:** 200 OK
**Response esperado:**
```json
{
    "version": "0.0.1-SNAPSHOT"
}
```

---

## Creación de Reservas (Requiere ROLE_CLIENTE)

### 3. Crear Reserva - Success

**Nombre:** Create Reservation - Success
**URL:** `http://localhost:8084/api/reservations`
**Método:** POST
**Headers:**
```
Content-Type: application/json
Authorization: Bearer [JWT_TOKEN_CLIENTE]
```
**Body:**
```json
{
    "idServicio": "[UUID-SERVICIO]",
    "idEmpleado": "[UUID-EMPLEADO]",
    "fechaHoraInicio": "2026-06-01T10:00:00Z",
    "comentarios": "Llegaré 5 minutos antes"
}
```
**Código esperado:** 201 Created
**Response esperado:**
```json
{
    "idReserva": "[UUID-NUEVA-RESERVA]",
    "idCliente": "[UUID-CLIENTE]",
    "clienteNombre": "Carlos Pérez",
    "clienteEmail": "carlos@ejemplo.com",
    "idServicio": "[UUID-SERVICIO]",
    "servicioNombre": "Corte de Cabello",
    "duracionMinutos": 30,
    "idEmpleado": "[UUID-EMPLEADO]",
    "empleadoNombre": "María García",
    "idProveedor": "[UUID-PROVEEDOR]",
    "proveedorNombre": "Salón Bella Vida",
    "fechaHoraInicio": "2026-06-01T10:00:00Z",
    "fechaHoraFin": "2026-06-01T10:30:00Z",
    "estado": "CONFIRMADA",
    "fechaCreacion": "2026-05-24T09:00:00Z",
    "comentarios": "Llegaré 5 minutos antes"
}
```

---

### 4. Crear Reserva - Sin Autenticación (401)

**Nombre:** Create Reservation - Unauthorized
**URL:** `http://localhost:8084/api/reservations`
**Método:** POST
**Headers:**
```
Content-Type: application/json
```
**Body:**
```json
{
    "idServicio": "[UUID-SERVICIO]",
    "idEmpleado": "[UUID-EMPLEADO]",
    "fechaHoraInicio": "2026-08-20T10:00:00Z"
}
```
**Código esperado:** 401 Unauthorized
**Response esperado:**
```json
{
    "timestamp": "2026-05-24T12:00:00Z",
    "status": 401,
    "error": "Unauthorized",
    "codigoError": "AUTHENTICATION_REQUIRED",
    "message": "Debes iniciar sesión para hacer una reserva",
    "path": "/api/reservations"
}
```

---

### 5. Crear Reserva - Horario Occupado (409)

**Nombre:** Create Reservation - Slot Not Available
**URL:** `http://localhost:8084/api/reservations`
**Método:** POST
**Headers:**
```
Content-Type: application/json
Authorization: Bearer [JWT_TOKEN_CLIENTE]
```
**Body:**
```json
{
    "idServicio": "[UUID-SERVICIO]",
    "idEmpleado": "[UUID-EMPLEADO]",
    "fechaHoraInicio": "2026-08-20T10:00:00Z"
}
```
**Código esperado:** 409 Conflict
**Response esperado:**
```json
{
    "timestamp": "2026-05-24T12:00:00Z",
    "status": 409,
    "error": "Conflict",
    "codigoError": "RESERVATION_CONFLICT",
    "message": "Este horario ya no está disponible",
    "path": "/api/reservations"
}
```

---

### 6. Crear Reserva - Datos Inválidos (400)

**Nombre:** Create Reservation - Invalid Data
**URL:** `http://localhost:8084/api/reservations`
**Método:** POST
**Headers:**
```
Content-Type: application/json
Authorization: Bearer [JWT_TOKEN_CLIENTE]
```
**Body:**
```json
{
    "idServicio": null,
    "idEmpleado": null,
    "fechaHoraInicio": null
}
```
**Código esperado:** 400 Bad Request
**Response esperado:**
```json
{
    "timestamp": "2026-05-24T12:00:00Z",
    "status": 400,
    "error": "Bad Request",
    "codigoError": "VALIDATION_ERROR",
    "message": "El ID del servicio es obligatorio, El ID del empleado es obligatorio, La fecha y hora de inicio es obligatoria",
    "path": "/api/reservations"
}
```

---

### 7. Crear Reserva - Fecha Pasada (400)

**Nombre:** Create Reservation - Past Date
**URL:** `http://localhost:8084/api/reservations`
**Método:** POST
**Headers:**
```
Content-Type: application/json
Authorization: Bearer [JWT_TOKEN_CLIENTE]
```
**Body:**
```json
{
    "idServicio": "[UUID-SERVICIO]",
    "idEmpleado": "[UUID-EMPLEADO]",
    "fechaHoraInicio": "2024-01-01T10:00:00Z"
}
```
**Código esperado:** 400 Bad Request
**Response esperado:**
```json
{
    "timestamp": "2026-05-24T12:00:00Z",
    "status": 400,
    "error": "Bad Request",
    "codigoError": "VALIDATION_ERROR",
    "message": "La fecha y hora de la reserva debe ser futura",
    "path": "/api/reservations"
}
```

---

## Consulta de Reservas

### 8. Obtener Reserva por ID - Success

**Nombre:** Get Reservation by ID - Success
**URL:** `http://localhost:8084/api/reservations/[UUID-RESERVA]`
**Método:** GET
**Headers:**
```
Content-Type: application/json
Authorization: Bearer [JWT_TOKEN]
```
**Código esperado:** 200 OK
**Response esperado:**
```json
{
    "idReserva": "[UUID-RESERVA]",
    "idCliente": "[UUID-CLIENTE]",
    "clienteNombre": "Carlos Pérez",
    "clienteEmail": "carlos@ejemplo.com",
    "idServicio": "[UUID-SERVICIO]",
    "servicioNombre": "Corte de Cabello",
    "duracionMinutos": 30,
    "idEmpleado": "[UUID-EMPLEADO]",
    "empleadoNombre": "María García",
    "idProveedor": "[UUID-PROVEEDOR]",
    "proveedorNombre": "Salón Bella Vida",
    "fechaHoraInicio": "2025-08-20T10:00:00Z",
    "fechaHoraFin": "2025-08-20T10:30:00Z",
    "estado": "CONFIRMADA",
    "fechaCreacion": "2025-06-01T09:00:00Z",
    "comentarios": null
}
```

---

### 9. Obtener Reserva por ID - No Encontrada (404)

**Nombre:** Get Reservation by ID - Not Found
**URL:** `http://localhost:8084/api/reservations/00000000-0000-0000-0000-000000000000`
**Método:** GET
**Headers:**
```
Content-Type: application/json
Authorization: Bearer [JWT_TOKEN]
```
**Código esperado:** 404 Not Found
**Response esperado:**
```json
{
    "timestamp": "2026-05-24T12:00:00Z",
    "status": 404,
    "error": "Not Found",
    "codigoError": "RESERVATION_NOT_FOUND",
    "message": "Reserva no encontrada con ID: 00000000-0000-0000-0000-000000000000",
    "path": "/api/reservations/00000000-0000-0000-0000-000000000000"
}
```

---

### 10. Listar Mis Reservas - Success

**Nombre:** Get My Reservations - Success
**URL:** `http://localhost:8084/api/reservations/my`
**Método:** GET
**Headers:**
```
Content-Type: application/json
Authorization: Bearer [JWT_TOKEN_CLIENTE]
```
**Código esperado:** 200 OK
**Response esperado:**
```json
{
    "reservas": [
        {
            "idReserva": "[UUID-RESERVA-1]",
            "idCliente": "[UUID-CLIENTE]",
            "servicioNombre": "Corte de Cabello",
            "estado": "CONFIRMADA",
            "fechaHoraInicio": "2025-08-20T10:00:00Z"
        }
    ],
    "total": 1,
    "pagina": 0,
    "tamanioPagina": 10,
    "tieneSiguiente": false
}
```

---

### 11. Listar Mis Reservas - Historial Vacío

**Nombre:** Get My Reservations - Empty
**URL:** `http://localhost:8084/api/reservations/my`
**Método:** GET
**Headers:**
```
Content-Type: application/json
Authorization: Bearer [JWT_TOKEN_CLIENTE_NUEVO]
```
**Código esperado:** 200 OK
**Response esperado:**
```json
{
    "reservas": [],
    "total": 0,
    "pagina": 0,
    "tamanioPagina": 10,
    "tieneSiguiente": false
}
```

---

### 12. Listar Mis Reservas - Filtrar por Estado

**Nombre:** Get My Reservations - Filter by Status
**URL:** `http://localhost:8084/api/reservations/my?estado=CONFIRMADA`
**Método:** GET
**Headers:**
```
Content-Type: application/json
Authorization: Bearer [JWT_TOKEN_CLIENTE]
```
**Código esperado:** 200 OK
**Response esperado:**
```json
{
    "reservas": [
        {
            "idReserva": "[UUID-RESERVA]",
            "estado": "CONFIRMADA",
            "fechaHoraInicio": "2025-08-20T10:00:00Z"
        }
    ],
    "total": 1,
    "pagina": 0,
    "tamanioPagina": 10,
    "tieneSiguiente": false
}
```

---

## Actualización de Reservas

### 13. Reprogramar Reserva - Success

**Nombre:** Update Reservation - Success
**URL:** `http://localhost:8084/api/reservations/[UUID-RESERVA]`
**Método:** PUT
**Headers:**
```
Content-Type: application/json
Authorization: Bearer [JWT_TOKEN_CLIENTE]
```
**Body:**
```json
{
    "fechaHoraInicio": "2025-08-21T14:00:00Z",
    "comentarios": "Cambié la fecha por asuntos personales"
}
```
**Código esperado:** 200 OK
**Response esperado:**
```json
{
    "idReserva": "[UUID-RESERVA]",
    "fechaHoraInicio": "2025-08-21T14:00:00Z",
    "fechaHoraFin": "2025-08-21T14:30:00Z",
    "estado": "CONFIRMADA",
    "comentarios": "Cambié la fecha por asuntos personales"
}
```

---

### 14. Reprogramar Reserva - Horario No Disponible (409)

**Nombre:** Update Reservation - Slot Not Available
**URL:** `http://localhost:8084/api/reservations/[UUID-RESERVA]`
**Método:** PUT
**Headers:**
```
Content-Type: application/json
Authorization: Bearer [JWT_TOKEN_CLIENTE]
```
**Body:**
```json
{
    "fechaHoraInicio": "2025-08-20T10:00:00Z"
}
```
**Código esperado:** 409 Conflict
**Response esperado:**
```json
{
    "timestamp": "2026-05-24T12:00:00Z",
    "status": 409,
    "error": "Conflict",
    "codigoError": "RESERVATION_CONFLICT",
    "message": "Horario no disponible",
    "path": "/api/reservations/[UUID-RESERVA]"
}
```

---

### 15. Reprogramar Reserva - No es Propietario (403)

**Nombre:** Update Reservation - Not Owner
**URL:** `http://localhost:8084/api/reservations/[UUID-RESERVA-OTRO]`
**Método:** PUT
**Headers:**
```
Content-Type: application/json
Authorization: Bearer [JWT_TOKEN_CLIENTE]
```
**Body:**
```json
{
    "fechaHoraInicio": "2025-08-21T14:00:00Z"
}
```
**Código esperado:** 403 Forbidden
**Response esperado:**
```json
{
    "timestamp": "2026-05-24T12:00:00Z",
    "status": 403,
    "error": "Forbidden",
    "codigoError": "RESERVATION_ACCESS_DENIED",
    "message": "No puedes modificar una reserva que no te pertenece",
    "path": "/api/reservations/[UUID-RESERVA-OTRO]"
}
```

---

### 16. Reprogramar Reserva Ya Cancelada (400)

**Nombre:** Update Reservation - Already Cancelled
**URL:** `http://localhost:8084/api/reservations/[UUID-RESERVA-CANCELADA]`
**Método:** PUT
**Headers:**
```
Content-Type: application/json
Authorization: Bearer [JWT_TOKEN_CLIENTE]
```
**Body:**
```json
{
    "fechaHoraInicio": "2025-08-21T14:00:00Z"
}
```
**Código esperado:** 400 Bad Request
**Response esperado:**
```json
{
    "timestamp": "2026-05-24T12:00:00Z",
    "status": 400,
    "error": "Bad Request",
    "codigoError": "INVALID_RESERVATION_STATE",
    "message": "Solo puedes reprogramar reservas activas",
    "path": "/api/reservations/[UUID-RESERVA-CANCELADA]"
}
```

---

## Cancelación de Reservas

### 17. Cancelar Reserva - Success

**Nombre:** Cancel Reservation - Success
**URL:** `http://localhost:8084/api/reservations/[UUID-RESERVA]`
**Método:** DELETE
**Headers:**
```
Content-Type: application/json
Authorization: Bearer [JWT_TOKEN_CLIENTE]
```
**Body:**
```json
{
    "comentariosCancelacion": "Ya no puedo asistir este día"
}
```
**Código esperado:** 200 OK
**Response esperado:**
```json
{
    "idReserva": "[UUID-RESERVA]",
    "estado": "CANCELADA",
    "fechaCancelacion": "2026-05-24T12:00:00Z"
}
```

---

### 18. Cancelar Reserva - No Existe (404)

**Nombre:** Cancel Reservation - Not Found
**URL:** `http://localhost:8084/api/reservations/00000000-0000-0000-0000-000000000000`
**Método:** DELETE
**Headers:**
```
Content-Type: application/json
Authorization: Bearer [JWT_TOKEN_CLIENTE]
```
**Código esperado:** 404 Not Found
**Response esperado:**
```json
{
    "timestamp": "2026-05-24T12:00:00Z",
    "status": 404,
    "error": "Not Found",
    "codigoError": "RESERVATION_NOT_FOUND",
    "message": "Reserva no encontrada con ID: 00000000-0000-0000-0000-000000000000",
    "path": "/api/reservations/00000000-0000-0000-0000-000000000000"
}
```

---

### 19. Cancelar Reserva - No es Propietario (403)

**Nombre:** Cancel Reservation - Not Owner
**URL:** `http://localhost:8084/api/reservations/[UUID-RESERVA-OTRO]`
**Método:** DELETE
**Headers:**
```
Content-Type: application/json
Authorization: Bearer [JWT_TOKEN_CLIENTE]
```
**Código esperado:** 403 Forbidden
**Response esperado:**
```json
{
    "timestamp": "2026-05-24T12:00:00Z",
    "status": 403,
    "error": "Forbidden",
    "codigoError": "RESERVATION_ACCESS_DENIED",
    "message": "No puedes cancelar una reserva que no te pertenece",
    "path": "/api/reservations/[UUID-RESERVA-OTRO]"
}
```

---

## Gestión de Estado (Proveedores/Admins)

### 20. Cambiar Estado - Completar Reserva (Requiere ROLE_PROVEEDOR)

**Nombre:** Change Reservation Status - Complete
**URL:** `http://localhost:8084/api/reservations/[UUID-RESERVA]/status`
**Método:** PATCH
**Headers:**
```
Content-Type: application/json
Authorization: Bearer [JWT_TOKEN_PROVEEDOR]
```
**Body:**
```json
{
    "estado": "COMPLETADA",
    "comentarios": "Servicio realizado satisfactoriamente"
}
```
**Código esperado:** 200 OK
**Response esperado:**
```json
{
    "idReserva": "[UUID-RESERVA]",
    "estado": "COMPLETADA"
}
```

---

### 21. Cambiar Estado - Marcar No-Show (Requiere ROLE_PROVEEDOR)

**Nombre:** Change Reservation Status - No Show
**URL:** `http://localhost:8084/api/reservations/[UUID-RESERVA]/status`
**Método:** PATCH
**Headers:**
```
Content-Type: application/json
Authorization: Bearer [JWT_TOKEN_PROVEEDOR]
```
**Body:**
```json
{
    "estado": "NO_SHOW",
    "comentarios": "Cliente no se presentó"
}
```
**Código esperado:** 200 OK
**Response esperado:**
```json
{
    "idReserva": "[UUID-RESERVA]",
    "estado": "NO_SHOW"
}
```

---

### 22. Cambiar Estado - Transición Inválida (400)

**Nombre:** Change Reservation Status - Invalid Transition
**URL:** `http://localhost:8084/api/reservations/[UUID-RESERVA]/status`
**Método:** PATCH
**Headers:**
```
Content-Type: application/json
Authorization: Bearer [JWT_TOKEN_PROVEEDOR]
```
**Body:**
```json
{
    "estado": "CONFIRMADA"
}
```
**Código esperado:** 400 Bad Request
**Response esperado:**
```json
{
    "timestamp": "2026-05-24T12:00:00Z",
    "status": 400,
    "error": "Bad Request",
    "codigoError": "INVALID_RESERVATION_STATE",
    "message": "No se puede cambiar el estado de una reserva COMPLETADA",
    "path": "/api/reservations/[UUID-RESERVA]/status"
}
```

---

## Consultas de Proveedor

### 23. Listar Reservas del Proveedor (Requiere ROLE_PROVEEDOR)

**Nombre:** Get Provider Reservations - Success
**URL:** `http://localhost:8084/api/reservations/provider`
**Método:** GET
**Headers:**
```
Content-Type: application/json
Authorization: Bearer [JWT_TOKEN_PROVEEDOR]
```
**Código esperado:** 200 OK
**Response esperado:**
```json
{
    "reservas": [
        {
            "idReserva": "[UUID-RESERVA-1]",
            "idCliente": "[UUID-CLIENTE-1]",
            "clienteNombre": "Carlos Pérez",
            "servicioNombre": "Corte de Cabello",
            "estado": "CONFIRMADA",
            "fechaHoraInicio": "2025-08-20T10:00:00Z"
        },
        {
            "idReserva": "[UUID-RESERVA-2]",
            "idCliente": "[UUID-CLIENTE-2]",
            "clienteNombre": "Ana López",
            "servicioNombre": "Manicura",
            "estado": "PENDIENTE",
            "fechaHoraInicio": "2025-08-20T11:00:00Z"
        }
    ],
    "total": 2,
    "pagina": 0,
    "tamanioPagina": 10,
    "tieneSiguiente": false
}
```

---

### 24. Listar Reservas de un Empleado (Requiere ROLE_PROVEEDOR)

**Nombre:** Get Employee Reservations - Success
**URL:** `http://localhost:8084/api/reservations/employee/[UUID-EMPLEADO]`
**Método:** GET
**Headers:**
```
Content-Type: application/json
Authorization: Bearer [JWT_TOKEN_PROVEEDOR]
```
**Código esperado:** 200 OK
**Response esperado:**
```json
{
    "reservas": [
        {
            "idReserva": "[UUID-RESERVA]",
            "idEmpleado": "[UUID-EMPLEADO]",
            "empleadoNombre": "María García",
            "estado": "CONFIRMADA",
            "fechaHoraInicio": "2025-08-20T10:00:00Z"
        }
    ],
    "total": 1,
    "pagina": 0,
    "tamanioPagina": 10,
    "tieneSiguiente": false
}
```

---

## Notas Importantes

### Obtención de Tokens JWT

Para obtener los tokens JWT necesarios para las pruebas que requieren autenticación:

1. **Token de Cliente:**
   - Registra un usuario con rol CLIENTE en el Auth Service
   - Inicia sesión: `POST http://localhost:8081/api/auth/login`
   - Usa el `accessToken` retornado

2. **Token de Proveedor:**
   - Registra un usuario con rol PROVEEDOR en el Auth Service
   - Inicia sesión: `POST http://localhost:8081/api/auth/login`
   - Usa el `accessToken` retornado

3. **Token de Admin:**
   - Registra un usuario con rol ADMIN en el Auth Service
   - Inicia sesión: `POST http://localhost:8081/api/auth/login`
   - Usa el `accessToken` retornado

### IDs de Prueba

Para las pruebas que requieren IDs específicos:
1. Ejecuta las pruebas de Auth Service para obtener usuarios registrados
2. Ejecuta las pruebas de Catalog Service para obtener servicios existentes
3. Ejecuta las pruebas de Schedule Service para obtener empleados existentes

### Precondiciones

Antes de ejecutar estas pruebas, asegúrate de:
1. Tener el Auth Service corriendo en `http://localhost:8081`
2. Tener el Catalog Service corriendo en `http://localhost:8082`
3. Tener el Schedule Service corriendo en `http://localhost:8083`
4. Tener el Reservation Service corriendo en `http://localhost:8084`
5. Tener usuarios registrados en el Auth Service con los roles necesarios (CLIENTE, PROVEEDOR, ADMIN)
6. Tener servicios creados en el Catalog Service
7. Tener empleados creados en el Schedule Service
8. Tener la tabla `reserva` creada en Supabase