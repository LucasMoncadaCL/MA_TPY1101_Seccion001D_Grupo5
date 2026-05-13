# Frontend Docs

## Objetivo

Este frontend implementa la seccion de Inventario para gestionar categorias de implementos, manteniendo un diseno coherente con el panel de referencia.

## Arquitectura

- `pages/InventoryCategoriesPage.tsx`: orquestacion de la pantalla.
- `hooks/useCategories.ts`: estado de negocio y operaciones CRUD.
- `services/categoryService.ts`: llamadas HTTP al backend.
- `services/apiClient.ts`: cliente Axios + parseo de errores API.
- `components/layout/*`: topbar, sidebar y shell.
- `components/categories/*`: tabla, tarjetas, modal de formulario y confirmaciones.
- `types/*`: contratos TypeScript.
- `styles/theme.css`: diseno global.

## Flujo CRUD

1. Carga inicial: `GET /api/v2/categories/gestion`
2. Asociacion por fila: `GET /api/v2/categories/{id}/associations`
3. Crear: `POST /api/v2/categories`
4. Editar: `PUT /api/v2/categories/{id}`
5. Desactivar:
   - Primer intento `PATCH .../deactivate?force=false`
   - Si responde `CATEGORY_HAS_ACTIVE_IMPLEMENTS`, se abre confirmacion y luego `force=true`
6. Eliminar: `DELETE /api/v2/categories/{id}`

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

- `VITE_API_BASE_URL`: URL publica del backend para el navegador.

## Despliegue

- Docker multi-stage (`node` build + `nginx` runtime).
- Compose recomendado desde raiz `Producto/` para levantar frontend + backend + postgres juntos.
