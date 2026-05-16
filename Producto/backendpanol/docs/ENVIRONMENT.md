# Entorno y Secrets del Backend

- Estado del documento: vigente
- Ultima verificacion: 2026-05-15
- Fuente de verdad: application.yaml, .env.local.example, docker compose vigentes

## Selector de entorno de BD

- `APP_DB_ENV=docker` -> usa `DB_DOCKER_*`
- `APP_DB_ENV=supabase` -> usa `DB_SUPABASE_*`

Si no se define, Spring usa perfil `docker` por defecto.

## Seguridad

- `APP_SECURITY_ENABLED=true` es el default vigente.
- Si `APP_SECURITY_ENABLED=false`, se activa configuracion sin autenticacion (solo para casos de debug controlado).

## Archivos de entorno

En `Producto/backendpanol`:
- `.env.local` (local no versionado)
- `.env.local.example` (plantilla versionada)
- `secrets/application-secrets.properties` (secretos runtime)
- `secrets/db_password.txt` (solo si usas postgres local fuera del compose de Producto)

En `Producto`:
- `.env` (variables para `Producto/docker-compose.yaml`)

## Variables clave

Comunes:
- `APP_PORT`
- `APP_DB_ENV`
- `APP_SECURITY_ENABLED`
- `APP_AUTH_MAX_FAILED_ATTEMPTS`
- `APP_AUTH_LOCK_MINUTES`
- `APP_AUTH_JWT_ISSUER`
- `APP_AUTH_JWT_EXPIRATION_SECONDS`
- `APP_AUTH_JWT_SECRET`

Docker DB:
- `DB_DOCKER_HOST`
- `DB_DOCKER_PORT`
- `DB_DOCKER_NAME`
- `DB_DOCKER_USER`
- `DB_DOCKER_PASSWORD`
- `DB_DOCKER_SSL_MODE`

Supabase:
- `DB_SUPABASE_HOST`
- `DB_SUPABASE_PORT`
- `DB_SUPABASE_NAME`
- `DB_SUPABASE_USER`
- `DB_SUPABASE_PASSWORD`
- `DB_SUPABASE_SSL_MODE`

jOOQ (build-time):
- `JOOQ_DB_URL`
- `JOOQ_DB_USER`
- `JOOQ_DB_PASSWORD`

## Compose y entorno

- `Producto/docker-compose.yaml` levanta `frontend + backend` (sin postgres local).
- `Producto/backendpanol/docker-compose.yaml` levanta backend only.

`APP_DB_ENV` define a que base conecta la app, no que servicios crea Docker Compose.

