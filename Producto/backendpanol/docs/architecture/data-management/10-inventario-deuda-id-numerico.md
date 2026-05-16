## Advertencia historica

Este documento conserva contexto tecnico de una etapa anterior. No debe usarse como guia operativa primaria sin contrastar con la documentacion vigente.

## Estado actual (vigente)

- Contratos publicos: solo /api/v2/**.
- Seguridad: permitAll solo en POST /api/v2/auth/login (+ health/info).
- Eventos: outbox operativo con estados PENDING/PROCESSED/FAILED.
- Compose principal: Producto/docker-compose.yaml (frontend + backend, sin postgres local).
- Estado del documento: historico
- Ultima verificacion: 2026-05-15
- Fuente de verdad: ver matriz canonica vigente y codigo fuente actual

# Inventario de Deuda ID NumÃ©rico (para corte UUID-only)

Fecha: 2026-05-10

## Backend: endpoints aÃºn no UUID-only

- `LocationController` (`/api/locations/**`): usa `Integer id`.
- `ImplementController` (`/api/implements/**`): usa `Integer id`.
- `StockController` (`/api/implements/{implementId}/stock/**`): usa `Integer implementId` e `Integer individualId`.
- `InventoryMovementController` (`/api/implements/**`): usa `Integer id`.
- `BarcodeLabelController` (`/api/implements/{implementId}/labels/**`): usa `Integer implementId`.
- `CategoriaController` (`/api/categorias/**`): usa `Integer id`.

## Backend: ya alineado (UUID-ready / v2)

- `AuthV2Controller` (`/api/v2/auth/**`).
- `UserAdminV2Controller` (`/api/v2/users/**`) con `UUID` en path.

## Frontend: deuda pendiente relevante

- `types` y `services` de inventario/categorÃ­as/ubicaciones/movimientos usan `number`.
- Rutas hash de inventario navegan por `id` numÃ©rico.
- Filtros/listados y detalle de implementos/stock usan claves numÃ©ricas.

## Frontend: alineado en este corte parcial

- Login/logout consumen `/api/v2/auth/**`.
- GestiÃ³n de usuarios Director consume `/api/v2/users/**` y usa `uuid`.
- Helper de auth sin dependencia operativa de `user_id`.

## Orden recomendado para terminar el corte

1. Implementos + stock (`/api/v2/implements/**`).
2. CategorÃ­as y ubicaciones (`/api/v2/categories`, `/api/v2/locations`).
3. Movimientos + etiquetas + detalle individual.
4. Ajuste final de rutas hash y tipos TS a UUID-only.


