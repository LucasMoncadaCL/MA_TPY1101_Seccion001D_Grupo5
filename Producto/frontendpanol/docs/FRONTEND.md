# Frontend Docs

- Estado del documento: vigente
- Ultima verificacion: 2026-05-15
- Fuente de verdad: servicios frontend + controllers backend V2

## Objetivo

Frontend para gestion operativa de inventario consumiendo API v2 del backend.

## Arquitectura

- `pages/InventoryCategoriesPage.tsx`
- `hooks/useCategories.ts`
- `services/categoryService.ts`
- `services/apiClient.ts`
- `components/layout/*`
- `components/categories/*`
- `types/*`

## Flujo CRUD de categorias

1. `GET /api/v2/categories/gestion`
2. `GET /api/v2/categories/{categoryUuid}/associations`
3. `POST /api/v2/categories`
4. `PUT /api/v2/categories/{categoryUuid}`
5. `PATCH /api/v2/categories/{categoryUuid}/deactivate?force=false|true`
6. `DELETE /api/v2/categories/{categoryUuid}`

## Errores

El frontend consume payload uniforme:

```json
{
  "code": "...",
  "message": "...",
  "timestamp": "..."
}
```

## Variables de entorno

- `VITE_API_BASE_URL` (ejemplo: `http://localhost:18080`)

## Despliegue local recomendado

Desde `Producto/`:
```bash
docker compose up --build
```

Este compose levanta frontend + backend.

