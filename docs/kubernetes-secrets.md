# Secrets para Kubernetes - MS-Reservation-Service

Este documento describe las variables necesarias para el archivo `secrets.yaml` requerido para el despliegue en Kubernetes.

## Ubicación del Archivo

El archivo `secrets.yaml` debe estar en la carpeta `kubernetes/` del proyecto:

```
Reservas-MS-Reservation-Service/
└── kubernetes/
    ├── k8s.yaml        # ConfigMap + Deployment + Service
    ├── secrets.yaml    # Secrets (NO subir a Git)
    └── .gitignore      # Ignora secrets.yaml
```

## Variables Requeridas

### 1. Base de Datos (Supabase)

| Variable | Descripción |
|----------|-------------|
| `DB_URL` | URL JDBC de PostgreSQL (Transaction Pooler) |
| `DB_USER` | Usuario de Supabase (`postgres.[PROJECT-REF]`) |
| `DB_PASSWORD` | Contraseña de la base de datos |

### 2. JWT

| Variable | Descripción |
|----------|-------------|
| `JWT_SECRET` | Secreto para validar tokens JWT |
| `JWT_ACCESS_EXPIRATION` | Tiempo de expiración access token (milisegundos) |
| `JWT_REFRESH_EXPIRATION` | Tiempo de expiración refresh token (milisegundos) |

> **IMPORTANTE:** El `JWT_SECRET` debe ser **el mismo valor** que en Auth-Service.

### 3. Email (Gmail SMTP)

| Variable | Descripción |
|----------|-------------|
| `EMAIL_HOST` | Servidor SMTP (`smtp.gmail.com`) |
| `EMAIL_PORT` | Puerto SMTP (`587`) |
| `EMAIL_USERNAME` | Dirección de correo Gmail |
| `EMAIL_PASSWORD` | App Password de Google |

### 4. URLs de Servicios

| Variable | Descripción |
|----------|-------------|
| `FRONTEND_URL` | URL base del frontend |
| `SERVICES_AUTH_URL` | URL del Auth Service (`http://auth-service:8081`) |
| `SERVICES_CATALOG_URL` | URL del Catalog Service (`http://catalog-service:8082`) |
| `SERVICES_SCHEDULE_URL` | URL del Schedule Service (`http://schedule-service:8083`) |

## Ejemplo de secrets.yaml

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: reservation-service-secrets
  namespace: default
type: Opaque
stringData:
  DB_URL: "jdbc:postgresql://..."
  DB_USER: "postgres.[PROJECT-REF]"
  DB_PASSWORD: "[TU-CONTRASEÑA]"
  JWT_SECRET: "[TU-JWT-SECRET]"
  JWT_ACCESS_EXPIRATION: "900000"
  JWT_REFRESH_EXPIRATION: "604800000"
  EMAIL_HOST: "smtp.gmail.com"
  EMAIL_PORT: "587"
  EMAIL_USERNAME: "tu@email.com"
  EMAIL_PASSWORD: "[APP-PASSWORD]"
  FRONTEND_URL: "https://tu-frontend.vercel.app"
  SERVICES_AUTH_URL: "http://auth-service:8081"
  SERVICES_CATALOG_URL: "http://catalog-service:8082"
  SERVICES_SCHEDULE_URL: "http://schedule-service:8083"
```

## Aplicar en Kubernetes

```bash
kubectl apply -f kubernetes/secrets.yaml
```

## Seguridad

⚠️ **NUNCA** subas el archivo `secrets.yaml` a Git. Ya está incluido en `.gitignore`.
