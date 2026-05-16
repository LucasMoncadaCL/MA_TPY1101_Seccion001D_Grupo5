- Estado del documento: historico
- Ultima verificacion: 2026-05-15
- Fuente de verdad: ver matriz canonica vigente y codigo fuente actual

# PSD-113 â€” Endpoint GET `/api/locations` (Backend) â€” HU-30

Fecha: 2026-04-29

## Objetivo

Implementar el endpoint `GET /api/locations` que retorne todas las ubicaciones registradas en la tabla `location` con los campos:

- `id`
- `name`

Condiciones:

- No filtra por estado (`active`) porque las ubicaciones son predefinidas y no se desactivan en esta etapa.
- Debe retornar un array vacÃ­o (`[]`) si no existen ubicaciones (sin error).
- Debe validar JWT cuando `app.security.enabled=true`.
- Accesible para todos los roles (catÃ¡logo de solo lectura).

## ImplementaciÃ³n

### API

- Se expone `GET /api/locations` en `LocationController`.
- La respuesta usa el DTO `LocationSelectorResponse` con `id` y `name`.
- Seguridad: el mÃ©todo queda con `@PreAuthorize("isAuthenticated()")`.
  - Con `app.security.enabled=false` (config actual), Spring Security estÃ¡ deshabilitado y el endpoint queda pÃºblico.
  - Con `app.security.enabled=true`, requiere JWT vÃ¡lido.

Archivos:

- `Producto/backendpanol/src/main/java/com/panol_project/backendpanol/modules/catalog/location/api/LocationController.java`
- `Producto/backendpanol/src/main/java/com/panol_project/backendpanol/modules/catalog/location/api/dto/LocationSelectorResponse.java`

### Servicio y repositorio

- `LocationService.listarSelector()` mantiene la obtenciÃ³n de todas las ubicaciones.
- `LocationJooqRepository.findAll()` ahora selecciona Ãºnicamente `LOCATION.ID` y `LOCATION.NAME` y ordena por `NAME`.

Archivos:

- `Producto/backendpanol/src/main/java/com/panol_project/backendpanol/modules/catalog/location/application/LocationService.java`
- `Producto/backendpanol/src/main/java/com/panol_project/backendpanol/modules/catalog/location/infrastructure/LocationJooqRepository.java`
- `Producto/backendpanol/src/main/java/com/panol_project/backendpanol/modules/catalog/location/domain/LocationOption.java`

## Contrato de respuesta

Ejemplo:

```json
[
  { "id": 1, "name": "Por definir" }
]
```

## CÃ³mo probar

Con el stack â€œProductoâ€ levantado:

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:18080/api/locations"
```

Si `app.security.enabled=true`:

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:18080/api/locations" `
  -Headers @{ Authorization = "Bearer $token" }
```

## Rebuild (Docker)

Se reconstruyÃ³ la imagen del backend con:

```powershell
cd Producto
docker compose build backend
docker compose up -d backend
```



## Advertencia historica

Este documento conserva contexto tecnico de una etapa anterior. No debe usarse como guia operativa primaria sin contrastar con la documentacion vigente.

## Estado actual (vigente)

- Contratos publicos: solo /api/v2/**.
- Seguridad: permitAll solo en POST /api/v2/auth/login (+ health/info).
- Eventos: outbox operativo con estados PENDING/PROCESSED/FAILED.
- Compose principal: Producto/docker-compose.yaml (frontend + backend, sin postgres local).

