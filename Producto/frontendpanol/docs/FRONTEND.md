# Frontend Docs

## Objetivo

Este frontend implementa la sección de Inventario para gestionar categorías de implementos, manteniendo un diseño coherente con el panel de referencia.

## Arquitectura

- `pages/InventoryCategoriesPage.tsx`: orquestación de la pantalla.
- `hooks/useCategories.ts`: estado de negocio y operaciones CRUD.
- `services/categoryService.ts`: llamadas HTTP al backend.
- `services/apiClient.ts`: cliente Axios + parseo de errores API.
- `components/layout/*`: topbar, sidebar y shell.
- `components/categories/*`: tabla, tarjetas, modal de formulario y confirmaciones.
- `types/*`: contratos TypeScript.
- `styles/theme.css`: diseño global.

## Flujo CRUD

1. Carga inicial: `GET /api/categorias/gestion`
2. Asociación por fila: `GET /api/categorias/{id}/asociaciones`
3. Crear: `POST /api/categorias`
4. Editar: `PUT /api/categorias/{id}`
5. Desactivar:
   - Primer intento `PATCH .../desactivar?force=false`
   - Si responde `CATEGORY_HAS_ACTIVE_IMPLEMENTS`, se abre confirmación y luego `force=true`
6. Eliminar: `DELETE /api/categorias/{id}`

## Manejo de errores

Se interpreta payload backend:

```json
{
  "code": "...",
  "message": "...",
  "timestamp": "..."
}
```

- `CATEGORY_NAME_DUPLICATE`: se muestra debajo del campo Nombre.
- Otros errores: banner superior del panel.

## Variables de entorno

- `VITE_API_BASE_URL`: URL pública del backend para el navegador.

## Despliegue

- Docker multi-stage (`node` build + `nginx` runtime).
- Compose recomendado desde raíz `Producto/` para levantar frontend + backend + postgres juntos.
