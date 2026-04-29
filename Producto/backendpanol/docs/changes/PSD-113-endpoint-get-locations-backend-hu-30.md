# PSD-113 — Endpoint GET `/api/locations` (Backend) — HU-30

Fecha: 2026-04-29

## Objetivo

Implementar el endpoint `GET /api/locations` que retorne todas las ubicaciones registradas en la tabla `location` con los campos:

- `id`
- `name`

Condiciones:

- No filtra por estado (`active`) porque las ubicaciones son predefinidas y no se desactivan en esta etapa.
- Debe retornar un array vacío (`[]`) si no existen ubicaciones (sin error).
- Debe validar JWT cuando `app.security.enabled=true`.
- Accesible para todos los roles (catálogo de solo lectura).

## Implementación

### API

- Se expone `GET /api/locations` en `LocationController`.
- La respuesta usa el DTO `LocationSelectorResponse` con `id` y `name`.
- Seguridad: el método queda con `@PreAuthorize("isAuthenticated()")`.
  - Con `app.security.enabled=false` (config actual), Spring Security está deshabilitado y el endpoint queda público.
  - Con `app.security.enabled=true`, requiere JWT válido.

Archivos:

- `Producto/backendpanol/src/main/java/com/panol_project/backendpanol/modules/catalog/location/api/LocationController.java`
- `Producto/backendpanol/src/main/java/com/panol_project/backendpanol/modules/catalog/location/api/dto/LocationSelectorResponse.java`

### Servicio y repositorio

- `LocationService.listarSelector()` mantiene la obtención de todas las ubicaciones.
- `LocationJooqRepository.findAll()` ahora selecciona únicamente `LOCATION.ID` y `LOCATION.NAME` y ordena por `NAME`.

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

## Cómo probar

Con el stack “Producto” levantado:

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:18080/api/locations"
```

Si `app.security.enabled=true`:

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:18080/api/locations" `
  -Headers @{ Authorization = "Bearer $token" }
```

## Rebuild (Docker)

Se reconstruyó la imagen del backend con:

```powershell
cd Producto
docker compose build backend
docker compose up -d backend
```

