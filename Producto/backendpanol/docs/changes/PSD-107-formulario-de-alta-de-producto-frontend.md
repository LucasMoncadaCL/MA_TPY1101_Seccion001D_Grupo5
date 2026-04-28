# PSD-107 - Formulario de alta de producto (Frontend)

Fecha: 2026-04-28

## Resumen

Se implemento el formulario de creacion de producto en React, accesible desde el panel de inventario del Coordinador, con validaciones cliente, manejo de errores inline, deshabilitacion de guardado durante la peticion y redireccion a la ficha del producto creado.

`location` queda formalmente obligatoria en frontend, alineado con la regla de negocio vigente y la base de datos actual.

## Alcance funcional implementado

### Acceso al formulario

- Boton `Nuevo implemento` en la vista de inventario.
- Apertura de modal de creacion desde `#/inventory/implementos`.

### Campos del formulario

Se implementaron todos los campos solicitados:

- `name`: input texto, obligatorio.
- `category`: selector obligatorio, cargado desde `GET /api/categories/active` (con fallback a `GET /api/categorias/active` para compatibilidad).
- `item_type`: selector obligatorio con opciones `consumable`, `reusable`, `individual`.
- `location`: selector obligatorio, cargado desde `GET /api/locations`.
- `description`: textarea opcional.
- `min_stock`: input numerico entero positivo, obligatorio.
- `observations`: textarea opcional.

### Validaciones y errores

- Validacion cliente antes de enviar:
  - nombre obligatorio,
  - categoria obligatoria,
  - tipo obligatorio,
  - ubicacion obligatoria,
  - stock minimo entero positivo.
- Errores de API mapeados a errores inline por campo.
- Error general de formulario mostrado dentro del modal (`fieldErrors.form`) cuando no se puede mapear a un campo.
- Se elimino el banner generico de error para creacion, para mantener la regla de errores inline.

### Flujo de guardado

- Boton `Guardar` deshabilitado mientras `saving = true`.
- Etiqueta del boton cambia a `Guardando...` durante la peticion.
- En creacion exitosa:
  - cierre del modal,
  - redireccion a `#/inventory/implementos/{id}`.

## Cambios tecnicos

### Frontend (componentes y paginas)

- Nuevo/actualizado modal de alta con campos y validaciones:
  - `frontendpanol/src/components/implements/ImplementFormModal.tsx`
- Integracion de creacion y redireccion desde inventario:
  - `frontendpanol/src/pages/InventoryItemsPage.tsx`
- Routing hash para ficha de implemento:
  - `frontendpanol/src/App.tsx`
- Vista de ficha para destino de redireccion:
  - `frontendpanol/src/pages/InventoryItemDetailPage.tsx`

### Servicios y tipos

- Creacion y obtencion de implementos:
  - `frontendpanol/src/services/implementService.ts`
- Carga de categorias activas:
  - `frontendpanol/src/services/activeCategoryService.ts`
- Carga de ubicaciones:
  - `frontendpanol/src/services/locationService.ts`
- Tipos de payload y detalle:
  - `frontendpanol/src/types/implement.ts`

### Estilos

- Ajustes de estilos para errores inline y ficha:
  - `frontendpanol/src/styles/theme.css`

## Criterios de aceptacion (estado)

1. Formulario accesible desde inventario para Coordinador: cumplido.
2. Campos requeridos y opcionales del alcance: cumplido.
3. Categoria desde endpoint de activas: cumplido.
4. Ubicacion desde endpoint de ubicaciones: cumplido (obligatoria).
5. Errores inline por campo: cumplido.
6. Guardar deshabilitado durante request: cumplido.
7. Redireccion a ficha al crear exitosamente: cumplido.
8. Referencias explicitas a HU en UI removidas: cumplido.

## Validacion manual sugerida

1. Ir a `#/inventory/implementos`.
2. Abrir `Nuevo implemento`.
3. Verificar carga de categorias y ubicaciones.
4. Enviar vacio y confirmar errores inline por campo.
5. Probar `min_stock` invalido (`0`, negativo, decimal) y validar mensaje inline.
6. Crear producto valido y confirmar:
   - boton en estado `Guardando...`,
   - redireccion automatica a ficha,
   - datos creados visibles en detalle.

## Verificacion tecnica

Compilacion frontend validada con:

```powershell
npm run build
```

Deploy local actualizado con:

```powershell
docker compose up -d --build --no-deps frontend
```
