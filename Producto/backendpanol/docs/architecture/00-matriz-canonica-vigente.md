# 00 - Matriz Canonica Vigente

- Estado del documento: vigente
- Ultima verificacion: 2026-05-15
- Fuente de verdad: controllers V2, `SecurityConfig`, `application.yaml`, compose files, `ArchitectureTest`

## Rutas publicas vigentes

- Base: `/api/v2/**`
- Auth: `/api/v2/auth/*`
- Users: `/api/v2/users/*`
- Categories: `/api/v2/categories/*`
- Locations: `/api/v2/locations/*`
- Implements/Stock: `/api/v2/implements/*`

## Seguridad vigente

- `permitAll`: `/actuator/health`, `/actuator/info`, `POST /api/v2/auth/login`.
- `denyAll`: `/internal/**`, `/api/v1/**`.
- Todo lo demas requiere autenticacion.
- `APP_SECURITY_ENABLED=true` por defecto.

## Flujo de datos vigente

1. SQL como estado canonico.
2. Evento outbox en misma transaccion cuando aplica.
3. Worker publica proyecciones/eventos a Mongo.
4. Reintentos y estado `PENDING/PROCESSED/FAILED` en `outbox_events`.

## Compose vigente

- `Producto/docker-compose.yaml`: frontend + backend (sin postgres local).
- `Producto/backendpanol/docker-compose.yaml`: backend only.

## Semantica documental

- `Estado del documento: vigente` => guia operativa actual.
- `Estado del documento: historico` => contexto pasado, no fuente operativa primaria.
- Todo documento debe incluir:
  - `Ultima verificacion`
  - `Fuente de verdad`
  - `Estado del documento`

