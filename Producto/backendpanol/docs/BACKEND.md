# Backend Docs

- Estado del documento: vigente
- Ultima verificacion: 2026-05-15
- Fuente de verdad: controllers V2, SecurityConfig, application.yaml

## Alcance

Guia operativa del backend para rutas publicas, seguridad, errores y convenciones de uso entre frontend y backend.

## API publica actual

Base publica: `/api/v2/**`

### Auth
- `POST /api/v2/auth/login`
- `POST /api/v2/auth/logout`

### Users
- `GET /api/v2/users`
- `POST /api/v2/users`
- `PUT /api/v2/users/{userUuid}`
- `PUT /api/v2/users/{userUuid}/role`
- `PATCH /api/v2/users/{userUuid}/active`
- `DELETE /api/v2/users/{userUuid}`

### Categories
- `GET /api/v2/categories/active`
- `GET /api/v2/categories/gestion`
- `GET /api/v2/categories/{categoryUuid}/associations`
- `POST /api/v2/categories`
- `PUT /api/v2/categories/{categoryUuid}`
- `PATCH /api/v2/categories/{categoryUuid}/deactivate`
- `DELETE /api/v2/categories/{categoryUuid}`

### Locations
- `GET /api/v2/locations`
- `GET /api/v2/locations/management`
- `POST /api/v2/locations`
- `PUT /api/v2/locations/{locationUuid}`
- `PATCH /api/v2/locations/{locationUuid}/active`

### Implements
- `GET /api/v2/implements`
- `GET /api/v2/implements/{implementUuid}`
- `POST /api/v2/implements`
- `PUT /api/v2/implements/{implementUuid}`
- `PATCH /api/v2/implements/{implementUuid}/active`

### Stock y movimientos
- `GET /api/v2/implements/movements`
- `POST /api/v2/implements/{implementUuid}/movements`
- `GET /api/v2/implements/{implementUuid}/stock`
- `POST /api/v2/implements/{implementUuid}/stock/entries`
- `POST /api/v2/implements/{implementUuid}/stock/movements`
- `PUT /api/v2/implements/{implementUuid}/stock/individuals/{individualUuid}`
- `GET /api/v2/implements/{implementUuid}/labels/pdf`

## Seguridad vigente

- `permitAll`: solo `POST /api/v2/auth/login` + `/actuator/health` y `/actuator/info`.
- Rutas bloqueadas: `/api/v1/**` y `/internal/**`.
- Resto de rutas: autenticadas.

## Formato de error publico

```json
{
  "code": "CATEGORY_NAME_DUPLICATE",
  "message": "Ya existe una categoria con el nombre 'Reactivos'",
  "timestamp": "2026-05-15T15:00:00Z"
}
```

## Nota de compatibilidad

No se deben usar rutas legacy (`/api/categorias`, `/api/implements`, `/api/v1/**`) en clientes nuevos ni en documentacion operativa vigente.

