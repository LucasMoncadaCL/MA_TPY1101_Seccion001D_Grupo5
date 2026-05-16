- Estado del documento: historico
- Ultima verificacion: 2026-05-15
- Fuente de verdad: ver matriz canonica vigente y codigo fuente actual

# PSD-95 â€” Implementos: `location_id` obligatorio + listado real (sin hardcode)

Fecha: 2026-04-27

## Resumen

Se eliminÃ³ el listado hardcodeado de implementos en el frontend y se habilitÃ³ la carga desde backend/BD.
AdemÃ¡s, se incorporÃ³ `location_id` como campo **obligatorio** al crear/editar un implemento y se agregÃ³ un endpoint para poblar el selector de ubicaciones.

## Backend (Spring Boot)

### Cambios funcionales

- `POST /api/implements` y `PUT /api/implements/{id}` ahora requieren `location_id`:
  - Si `location_id` viene ausente o `null` â†’ `400 Bad Request` (validaciÃ³n).
  - Si `location_id` no existe en BD â†’ `400 Bad Request`.
- Endpoint nuevo `GET /api/locations`:
  - Retorna `[{ id, name, description }]` ordenado por `name`.
- Endpoint nuevo `GET /api/implements`:
  - Retorna listado para la tabla del frontend con `category` (nullable) y `location` (no nullable).

### Contratos (alto nivel)

- `GET /api/locations`
  - 200: `[{ "id": 1, "name": "Por definir", "description": "..." }]`

- `GET /api/implements`
  - 200:
    ```json
    [
      {
        "id": 1,
        "name": "Guantes latex",
        "category": { "id": 2, "name": "Herramientas", "active": true },
        "location": { "id": 1, "name": "Por definir", "description": "Ubicacion pendiente" }
      }
    ]
    ```

- `POST /api/implements` / `PUT /api/implements/{id}`
  - Body incluye:
    - `name` (string)
    - `category_id` (number | null)
    - `location_id` (number) **obligatorio**

### Persistencia / migraciones

- Flyway: `Producto/backendpanol/src/main/resources/db/migration/V2__locations_and_implement_location_fk.sql`
  - Crea `location` (si no existe) con:
    - `name VARCHAR(20) NOT NULL`
    - `description VARCHAR(200)`
  - Inserta una ubicaciÃ³n por defecto (`Por definir`) si no hay ninguna.
  - Agrega `implement.location_id` (si no existe), hace backfill y lo deja `NOT NULL`.
  - Agrega FK `implement_location_fk` (`ON DELETE RESTRICT`).

## Frontend (React + TypeScript)

### Cambios funcionales

- Pantalla `#/inventory/implementos` ahora:
  - Carga implementos reales via `GET /api/implements`.
  - Muestra estado de carga y refresca el listado despuÃ©s de crear/editar.
- Modal de alta/ediciÃ³n de implemento:
  - Agrega selector de **UbicaciÃ³n** (obligatorio), consumiendo `GET /api/locations`.
  - Mantiene selector de **CategorÃ­a** (opcional) consumiendo `GET /api/categories/active`.
  - EnvÃ­a `location_id` en `POST /api/implements` y `PUT /api/implements/{id}`.

### Archivos relevantes

- Backend:
  - `Producto/backendpanol/src/main/java/com/panol_project/backendpanol/modules/catalog/location/**`
  - `Producto/backendpanol/src/main/java/com/panol_project/backendpanol/modules/catalog/implement/**`
  - `Producto/backendpanol/src/main/resources/db/migration/V2__locations_and_implement_location_fk.sql`
- Frontend:
  - `Producto/frontendpanol/src/pages/InventoryItemsPage.tsx`
  - `Producto/frontendpanol/src/components/implements/ImplementFormModal.tsx`
  - `Producto/frontendpanol/src/components/implements/ImplementEditModal.tsx`
  - `Producto/frontendpanol/src/services/implementService.ts`
  - `Producto/frontendpanol/src/services/locationService.ts`
  - `Producto/frontendpanol/src/types/implement.ts`
  - `Producto/frontendpanol/src/types/location.ts`

## CÃ³mo probar

1) Verificar endpoints (backend):
- `GET http://localhost:18080/api/locations`
- `GET http://localhost:18080/api/implements`

2) UI (frontend):
- Abrir `http://localhost:18081/#/inventory/implementos`
- Crear implemento:
  - seleccionar ubicaciÃ³n obligatoria
  - categorÃ­a opcional
  - guardar y validar que aparezca en la tabla



## Advertencia historica

Este documento conserva contexto tecnico de una etapa anterior. No debe usarse como guia operativa primaria sin contrastar con la documentacion vigente.

## Estado actual (vigente)

- Contratos publicos: solo /api/v2/**.
- Seguridad: permitAll solo en POST /api/v2/auth/login (+ health/info).
- Eventos: outbox operativo con estados PENDING/PROCESSED/FAILED.
- Compose principal: Producto/docker-compose.yaml (frontend + backend, sin postgres local).

