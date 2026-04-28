# PSD-108 - Confirmacion visual de stock inicial en 0 (Frontend)

Fecha: 2026-04-28

## Resumen

Se implemento la confirmacion visual para evitar confusion operativa tras crear un producto:

- En el formulario de alta, bajo `Stock minimo`, se muestra un mensaje preventivo antes de guardar.
- En la ficha del producto recien creado, se muestra `Stock disponible: 0` con un badge que sugiere la siguiente accion operativa: `Ingresa un lote`.

## Estado de cumplimiento

La subtarea queda **100% cumplida** respecto a la descripcion solicitada.

## Requerimientos y evidencia

### 1) Mensaje en formulario antes de guardar

Texto implementado bajo el campo `Stock minimo`:

`El stock inicial del producto será 0. Para agregar unidades usa el ingreso de lote.`

Archivo:

- `frontendpanol/src/components/implements/ImplementFormModal.tsx`

### 2) Indicador visual en ficha del producto recien creado

En la ficha se muestra:

- `Stock disponible: 0`
- badge `Ingresa un lote`

Archivo:

- `frontendpanol/src/pages/InventoryItemDetailPage.tsx`

### 3) Contexto "recien creado" sin afectar otras fichas

Para mostrar el indicador solo cuando corresponde al flujo inmediato post-creacion:

- Se guarda el id del implemento creado en `sessionStorage` al crear.
- La ficha compara ese id con la ruta actual.
- Si coincide, muestra el indicador y limpia el marcador (one-shot).

Archivo:

- `frontendpanol/src/pages/InventoryItemsPage.tsx`

## Cambios tecnicos

### UI / Estilos

Se agregaron estilos para el badge y el bloque visual del hint de stock disponible:

- `badge--warn`
- `detail-stock-hint`

Archivo:

- `frontendpanol/src/styles/theme.css`

## Verificacion tecnica

Compilacion frontend validada con:

```powershell
npm run build
```

## Validacion manual sugerida

1. Ir a `#/inventory/implementos`.
2. Abrir `Nuevo implemento`.
3. Verificar que se muestra el texto informativo bajo `Stock minimo` antes de guardar.
4. Crear implemento valido.
5. Confirmar redireccion a ficha del producto creado.
6. Confirmar visual en ficha:
   - `Stock disponible: 0`
   - badge `Ingresa un lote`.
7. Abrir ficha de otro producto no recien creado y confirmar que no aparece ese indicador.
