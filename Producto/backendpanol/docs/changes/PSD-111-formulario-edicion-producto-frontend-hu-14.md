# PSD-111 - Formulario de edición de producto desde ficha (Frontend) (HU-14)

Fecha: 2026-04-29

## Resumen

Se implementó el formulario de edición de producto accesible desde la **ficha del producto**.

El flujo permite:

- cargar los datos actuales del producto (`GET /api/implements/{id}`) y **pre-poblar** el formulario
- editar y guardar los campos requeridos por HU-14 mediante `PUT /api/implements/{id}`
- mostrar errores inline por campo
- deshabilitar el botón Guardar mientras la petición está en curso
- mostrar confirmación inline en la ficha y **permanecer** en la misma pantalla tras guardar

## Alcance funcional implementado

### 1) Acceso desde la ficha

En la ficha del producto se agregó el botón `Editar`, el cual abre el formulario de edición como modal.

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

### 3) Categoría inactiva (regla de guardado)

Si la categoría actual del producto viene inactiva desde el backend (`category.active = false`):

- se muestra en el modal con badge `(inactiva)`
- se impide guardar mientras el usuario no seleccione una categoría activa

Archivo:

- `Producto/frontendpanol/src/components/implements/ImplementEditModal.tsx`

### 4) Errores inline y UX de envío

- errores por campo se muestran con `field-error`
- el botón `Guardar` se deshabilita mientras `saving=true` para evitar doble envío

Archivo:

- `Producto/frontendpanol/src/components/implements/ImplementEditModal.tsx`

### 5) Confirmación inline y permanencia en ficha

Al guardar exitosamente:

- se cierra el modal
- se actualiza el estado del producto mostrado en la ficha
- se muestra confirmación inline (`success-banner`)
- no se navega fuera de la ficha

Archivo:

- `Producto/frontendpanol/src/pages/InventoryItemDetailPage.tsx`

## Cambios técnicos

### Tipos y servicios

Se amplió el payload del update para soportar todos los campos del backend (HU-14) y se ajustó el servicio `updateImplement` para retornar el recurso actualizado.

Archivos:

- `Producto/frontendpanol/src/types/implement.ts`
- `Producto/frontendpanol/src/services/implementService.ts`

### Reuso del modal en listado y ficha

El modal de edición ahora se alimenta por `implementId` y obtiene detalle por API, permitiendo reuso desde:

- ficha (`InventoryItemDetailPage`)
- listado (`InventoryItemsPage`)

Archivos:

- `Producto/frontendpanol/src/pages/InventoryItemDetailPage.tsx`
- `Producto/frontendpanol/src/pages/InventoryItemsPage.tsx`
- `Producto/frontendpanol/src/components/implements/ImplementEditModal.tsx`

## Cómo probar

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
3. Cambiar campos (incluyendo `Tipo`, `Stock mínimo`, `Observaciones`)
4. Click `Guardar`:
   - el botón se deshabilita y muestra “Guardando...”
   - al éxito: aparece confirmación en la ficha y los valores quedan actualizados

### 2) Caso “categoría inactiva”

Precondición: producto cuyo `category.active=false`.

1. Abrir ficha y `Editar`
2. Ver badge `(inactiva)`
3. Verificar que `Guardar` queda deshabilitado hasta seleccionar una categoría activa

## Notas

- La restricción de “categoría inactiva” se aplica en base a la respuesta del backend (`category.active`).
- La UI no valida explícitamente el rol “COORDINADOR” para mostrar el botón; la autorización se espera del backend cuando `app.security.enabled=true`.
