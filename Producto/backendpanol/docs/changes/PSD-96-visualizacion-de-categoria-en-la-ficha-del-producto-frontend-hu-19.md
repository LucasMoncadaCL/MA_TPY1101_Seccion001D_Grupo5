- Estado del documento: historico
- Ultima verificacion: 2026-05-15
- Fuente de verdad: ver matriz canonica vigente y codigo fuente actual

# PSD-96 - Visualizacion de categoria en la ficha del producto (HU-19)

Fecha: 2026-04-28

## Resumen

Se actualizo la ficha de producto para mostrar la categoria asignada al implemento de forma consistente y sin llamadas adicionales.

El backend expone en `GET /api/implements/{id}` el objeto `category` con `{ id, name, active }` (nullable) para permitir:

- Mostrar el nombre cuando la categoria esta asignada y activa.
- Mostrar "Sin categoria" cuando no hay categoria.
- Mantener el nombre con estado `active=false` cuando la categoria fue desactivada posteriormente, para dar contexto historico.

Adicionalmente, el endpoint de detalle ahora incluye `location` con `{ id, name, description }` para que el frontend pueda renderizar la ficha completa sin depender del listado.

## Cambios funcionales

### 1) Detalle de implemento incluye categoria (nullable)

Endpoint: `GET /api/implements/{id}`

- `category = null` cuando `category_id` es `NULL`.
- `category = { id, name, active }` cuando existe categoria asociada (incluye casos con `active=false`).

Ejemplo (sin categoria):

```json
{
  "id": 1,
  "name": "Guantes latex",
  "category": null,
  "categoryId": null
}
```

Ejemplo (categoria inactiva):

```json
{
  "id": 1,
  "name": "Guantes latex",
  "category": { "id": 2, "name": "Reactivos", "active": false },
  "categoryId": 2
}
```

### 2) Detalle de implemento incluye ubicacion

Endpoint: `GET /api/implements/{id}`

Incluye:

- `location = { id, name, description }`

Esto evita que la UI tenga que consultar el listado para resolver el nombre de ubicacion.

### 3) UI: render de categoria en la ficha

En la seccion de atributos de la ficha:

- Si `category` existe y `active=true`: se muestra el nombre.
- Si `category` es `null`: se muestra "Sin categoria" en gris.
- Si `category` existe y `active=false`: se muestra el nombre y un badge `[Inactiva]`.

## Cambios tecnicos

### Backend

- Se agrego `findSummaryById(id)` al `ImplementRepository` para reutilizar el join (implement + category + location) del listado en la vista de detalle.
- `ImplementResponse` se extendio con:
  - `category: { id, name, active } | null`
  - `location: { id, name, description } | null`
- `POST /api/implements` y `PUT /api/implements/{id}` devuelven el mismo shape de `ImplementResponse` (incluyendo `category` y `location`) para mantener consistencia de contrato.

### Frontend

- `ImplementDetail` se actualizo para incluir `category` y `location`.
- La ficha dejo de depender del listado (`GET /api/implements`) para resolver categoria/ubicacion y se apoya solo en `GET /api/implements/{id}`.
- Se agrego una clase CSS utilitaria para texto atenuado (`.text-muted`) usada para "Sin categoria".

## Archivos modificados

- Backend:
  - `Producto/backendpanol/src/main/java/com/panol_project/backendpanol/modules/catalog/implement/domain/ImplementRepository.java`
  - `Producto/backendpanol/src/main/java/com/panol_project/backendpanol/modules/catalog/implement/infrastructure/ImplementJooqRepository.java`
  - `Producto/backendpanol/src/main/java/com/panol_project/backendpanol/modules/catalog/implement/application/ImplementService.java`
  - `Producto/backendpanol/src/main/java/com/panol_project/backendpanol/modules/catalog/implement/api/ImplementController.java`
  - `Producto/backendpanol/src/main/java/com/panol_project/backendpanol/modules/catalog/implement/api/dto/ImplementResponse.java`
  - `Producto/backendpanol/docs/modules/catalog-implement.md`
- Frontend:
  - `Producto/frontendpanol/src/types/implement.ts`
  - `Producto/frontendpanol/src/pages/InventoryItemDetailPage.tsx`
  - `Producto/frontendpanol/src/styles/theme.css`

## Como validar manualmente

1) Backend:

- Consultar detalle:
  - `GET http://localhost:18080/api/implements/{id}`
- Verificar que la respuesta incluya:
  - `category` (null o `{ id, name, active }`)
  - `location` (`{ id, name, description }`)

2) Frontend:

- Abrir `http://localhost:18081`
- Entrar a la ficha de un producto y validar:
  - Categoria activa: nombre sin badge.
  - Sin categoria: texto "Sin categoria" en gris.
  - Categoria inactiva: nombre + badge `[Inactiva]`.

## Notas

- No se introdujeron cambios de seguridad/permisos en esta subtarea; se mantiene el comportamiento actual del proyecto.



## Advertencia historica

Este documento conserva contexto tecnico de una etapa anterior. No debe usarse como guia operativa primaria sin contrastar con la documentacion vigente.

## Estado actual (vigente)

- Contratos publicos: solo /api/v2/**.
- Seguridad: permitAll solo en POST /api/v2/auth/login (+ health/info).
- Eventos: outbox operativo con estados PENDING/PROCESSED/FAILED.
- Compose principal: Producto/docker-compose.yaml (frontend + backend, sin postgres local).

