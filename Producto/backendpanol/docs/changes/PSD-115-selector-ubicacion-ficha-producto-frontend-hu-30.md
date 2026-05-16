- Estado del documento: historico
- Ultima verificacion: 2026-05-15
- Fuente de verdad: ver matriz canonica vigente y codigo fuente actual

# PSD-115 â€” Selector de ubicaciÃ³n en ficha de producto (Frontend) â€” HU-30

Fecha: 2026-04-29

## Objetivo

En la ficha del producto:

- Mostrar la ubicaciÃ³n como campo editable mediante un selector desplegable poblado desde `GET /api/locations`.
- Si `display_location` retorna `"Prestado"`, mostrar ese valor como texto no editable con badge distintivo y **no permitir cambios** mientras existan unidades prestadas.
- Al guardar, consumir `PUT /api/implements/{id}` (HU-14) sin crear endpoints nuevos.

## ImplementaciÃ³n

### Vista ficha (`InventoryItemDetailPage`)

- Se incorpora carga de ubicaciones con `fetchLocations()` y se renderiza un `<select>` con botÃ³n `Guardar`.
- El guardado llama a `updateImplement(implementId, payload)` reutilizando el `PUT /api/implements/{id}`.
- Si `display_location === "Prestado"`, el selector se reemplaza por texto + badge y el cambio queda bloqueado.

Archivo:

- `Producto/frontendpanol/src/pages/InventoryItemDetailPage.tsx`

### Tipos

- Se aÃ±ade el campo opcional `display_location` en `ImplementDetail` para soportar el nuevo contrato del backend.

Archivo:

- `Producto/frontendpanol/src/types/implement.ts`

## Validaciones / UX

- El botÃ³n `Guardar` se deshabilita mientras se cargan ubicaciones o mientras la peticiÃ³n de guardado estÃ¡ en curso.
- Dado que el `PUT` requiere payload completo, si el implemento no tiene informaciÃ³n mÃ­nima vÃ¡lida (por ejemplo, `min_stock` invÃ¡lido), se muestra error indicando que primero debe editarse el producto desde el formulario completo.

## CÃ³mo probar

1) Levantar stack â€œProductoâ€.
2) Ir a `http://localhost:18081/#/inventory/implementos/1`.
3) Caso normal (sin prÃ©stamos):
   - Cambiar ubicaciÃ³n en el selector y presionar `Guardar`.
   - Esperar mensaje â€œUbicaciÃ³n actualizada correctamente.â€ y que la ficha permanezca en la misma pÃ¡gina.
4) Caso prestado:
   - Asegurar que el backend retorne `display_location = "Prestado"` (loaned > 0).
   - Verificar que se muestra badge â€œPrestadoâ€ y no aparece selector editable.



## Advertencia historica

Este documento conserva contexto tecnico de una etapa anterior. No debe usarse como guia operativa primaria sin contrastar con la documentacion vigente.

## Estado actual (vigente)

- Contratos publicos: solo /api/v2/**.
- Seguridad: permitAll solo en POST /api/v2/auth/login (+ health/info).
- Eventos: outbox operativo con estados PENDING/PROCESSED/FAILED.
- Compose principal: Producto/docker-compose.yaml (frontend + backend, sin postgres local).

