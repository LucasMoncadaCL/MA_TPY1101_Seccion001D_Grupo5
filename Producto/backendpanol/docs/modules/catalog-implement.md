# Modulo: catalog/implement

## Responsabilidad

Gestionar la creacion y edicion de implementos (productos) del catalogo, incluyendo la persistencia de `category_id` y las validaciones asociadas a categorias activas.

## Endpoints

Base path: `/api/implements`

- `POST /api/implements` (crear implemento; `category_id` opcional)
- `PUT /api/implements/{id}` (editar implemento; `category_id` opcional)
- `GET /api/implements/{id}` (obtener implemento)

### Respuesta de detalle

El endpoint `GET /api/implements/{id}` incluye:

- `category`: `null` o `{ id, name, active }` (si la categoria fue desactivada posteriormente, `active=false` igualmente se devuelve para dar contexto).
- `location`: `{ id, name, description }`

## Validaciones relevantes (PSD)

- Si `category_id` viene en el request, debe existir y estar activa.
- Si `category_id` es `null` o ausente, se persiste como `NULL` (permitido).

La validacion se reutiliza desde `CategoriaService.validarCategoriaActivaParaImplemento(...)`.

