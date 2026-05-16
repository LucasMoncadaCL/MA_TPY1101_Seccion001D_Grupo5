- Estado del documento: historico
- Ultima verificacion: 2026-05-15
- Fuente de verdad: ver matriz canonica vigente y codigo fuente actual

# PSD-93 - Campo selector de categoria en alta de producto (HU-13) - Integracion frontend

## Objetivo

Agregar al formulario de creacion de implemento/producto un selector de categoria que se alimente dinamicamente desde el backend, permitiendo que la categoria sea opcional (nullable).

## Comportamiento UI

- Al abrir el modal de "Nuevo implemento", el selector consulta categorias activas.
- Mientras carga, el selector muestra estado de carga.
- Primera opcion por defecto: "Sin categoria" (value vacio).
- Si no hay categorias activas, el selector queda deshabilitado y muestra "No hay categorias disponibles".

## Integracion API

- GET categorias activas:
  - Primario: `GET /api/categories/active`
  - Fallback: `GET /api/categorias/active`
- POST creacion implemento:
  - `POST /api/implements` incluyendo `category_id` como `number | null`.

## Archivos frontend relevantes

- Modal/formulario: `Producto/frontendpanol/src/components/implements/ImplementFormModal.tsx`
- Pagina HU-13 (para probar el modal): `Producto/frontendpanol/src/pages/InventoryItemsPage.tsx`
- Routing simple por hash: `Producto/frontendpanol/src/App.tsx`
- Servicio GET categorias activas (con fallback): `Producto/frontendpanol/src/services/activeCategoryService.ts`
- Servicio POST implemento: `Producto/frontendpanol/src/services/implementService.ts`
- Types: `Producto/frontendpanol/src/types/categoryActive.ts`, `Producto/frontendpanol/src/types/implement.ts`
- Estilos (select/hints/banners): `Producto/frontendpanol/src/styles/theme.css`

## Nota sobre seguridad (temporal)

Para poder probar sin JWT, el backend soporta deshabilitar seguridad via `APP_SECURITY_ENABLED=false` (default). Cuando se habilite seguridad, el frontend ya envia `Authorization: Bearer <token>` si existe `localStorage.access_token`.



## Advertencia historica

Este documento conserva contexto tecnico de una etapa anterior. No debe usarse como guia operativa primaria sin contrastar con la documentacion vigente.

## Estado actual (vigente)

- Contratos publicos: solo /api/v2/**.
- Seguridad: permitAll solo en POST /api/v2/auth/login (+ health/info).
- Eventos: outbox operativo con estados PENDING/PROCESSED/FAILED.
- Compose principal: Producto/docker-compose.yaml (frontend + backend, sin postgres local).

