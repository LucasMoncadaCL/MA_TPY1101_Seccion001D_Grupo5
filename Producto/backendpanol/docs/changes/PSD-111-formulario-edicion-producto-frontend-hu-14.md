- Estado del documento: historico
- Ultima verificacion: 2026-05-15
- Fuente de verdad: ver matriz canonica vigente y codigo fuente actual

# PSD-111 - Formulario de ediciÃ³n de producto desde ficha (Frontend) (HU-14)

Fecha: 2026-04-29

## Resumen

Se implementÃ³ el formulario de ediciÃ³n de producto accesible desde la **ficha del producto**.

El flujo permite:

- cargar los dtos actuales del producto (`GET /api/implements/{id}`) y **pre-poblar** el formulario
- editar y guardar los campos requeridos por HU-14 mediante `PUT /api/implements/{id}`
- mostrar errores inline por campo
- deshabilitar el botÃ³n Guardar mientras la peticiÃ³n estÃ¡ en curso
- mostrar confirmaciÃ³n inline en la ficha y **permanecer** en la misma pantalla tras guardar

## Alcance funcional implementado

### 1) Acceso desde la ficha

En la ficha del producto se agregÃ³ el botÃ³n `Editar`, el cual abre el formulario de ediciÃ³n como modal.

Archivo:

- `Producto/frontendpanol/src/pages/InventoryItemDetailPage.tsx`

### 2) Precarga de datos (prefill)

Al abrir el modal:

- se ejecuta `GET /api/implements/{id}`
- se pre-cargan todos los campos editables:
  - `name`
  - `category_id`
  - `item_type`
  - `location_id`
  - `description`
  - `min_stock`
  - `observations`

Archivo:

- `Producto/frontendpanol/src/components/implements/ImplementEditModal.tsx`

### 3) CategorÃ­a inactiva (regla de guardado)

Si la categorÃ­a actual del producto viene inactiva desde el backend (`category.active = false`):

- se muestra en el modal con badge `(inactiva)`
- se impide guardar mientras el usuario no seleccione una categorÃ­a activa

Archivo:

- `Producto/frontendpanol/src/components/implements/ImplementEditModal.tsx`

### 4) Errores inline y UX de envÃ­o

- errores por campo se muestran con `field-error`
- el botÃ³n `Guardar` se deshabilita mientras `saving=true` para evitar doble envÃ­o

Archivo:

- `Producto/frontendpanol/src/components/implements/ImplementEditModal.tsx`

### 5) ConfirmaciÃ³n inline y permanencia en ficha

Al guardar exitosamente:

- se cierra el modal
- se actualiza el estado del producto mostrado en la ficha
- se muestra confirmaciÃ³n inline (`success-banner`)
- no se navega fuera de la ficha

Archivo:

- `Producto/frontendpanol/src/pages/InventoryItemDetailPage.tsx`

## Cambios tÃ©cnicos

### Tipos y servicios

Se ampliÃ³ el payload del update para soportar todos los campos del backend (HU-14) y se ajustÃ³ el servicio `updateImplement` para retornar el recurso actualizado.

Archivos:

- `Producto/frontendpanol/src/types/implement.ts`
- `Producto/frontendpanol/src/services/implementService.ts`

### Reuso del modal en listado y ficha

El modal de ediciÃ³n ahora se alimenta por `implementId` y obtiene detalle por API, permitiendo reuso desde:

- ficha (`InventoryItemDetailPage`)
- listado (`InventoryItemsPage`)

Archivos:

- `Producto/frontendpanol/src/pages/InventoryItemDetailPage.tsx`
- `Producto/frontendpanol/src/pages/InventoryItemsPage.tsx`
- `Producto/frontendpanol/src/components/implements/ImplementEditModal.tsx`

## CÃ³mo probar

### 1) Con el stack Docker (recomendado)

Levantar stack:

```powershell
cd Producto
docker compose --env-file .\\backendpanol\\.env.local up -d
```

Abrir ficha:

- `http://localhost:18081/#/inventory/implementos/<id>`

Flujo:

1. Click `Editar`
2. Verificar prefill (datos actuales)
3. Cambiar campos (incluyendo `Tipo`, `Stock mÃ­nimo`, `Observaciones`)
4. Click `Guardar`:
   - el botÃ³n se deshabilita y muestra â€œGuardando...â€
   - al Ã©xito: aparece confirmaciÃ³n en la ficha y los valores quedan actualizados

### 2) Caso â€œcategorÃ­a inactivaâ€

PrecondiciÃ³n: producto cuyo `category.active=false`.

1. Abrir ficha y `Editar`
2. Ver badge `(inactiva)`
3. Verificar que `Guardar` queda deshabilitado hasta seleccionar una categorÃ­a activa

## Notas

- La restricciÃ³n de â€œcategorÃ­a inactivaâ€ se aplica en base a la respuesta del backend (`category.active`).
- La UI no valida explÃ­citamente el rol â€œCOORDINADORâ€ para mostrar el botÃ³n; la autorizaciÃ³n se espera del backend cuando `app.security.enabled=true`.


## Advertencia historica

Este documento conserva contexto tecnico de una etapa anterior. No debe usarse como guia operativa primaria sin contrastar con la documentacion vigente.

## Estado actual (vigente)

- Contratos publicos: solo /api/v2/**.
- Seguridad: permitAll solo en POST /api/v2/auth/login (+ health/info).
- Eventos: outbox operativo con estados PENDING/PROCESSED/FAILED.
- Compose principal: Producto/docker-compose.yaml (frontend + backend, sin postgres local).

