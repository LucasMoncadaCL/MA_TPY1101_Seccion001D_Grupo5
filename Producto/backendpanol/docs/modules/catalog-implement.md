# Modulo: catalog/implement

## Responsabilidad

Gestionar la creacion y edicion de implementos (productos) del catalogo, incluyendo la persistencia de `category_id` y las validaciones asociadas a categorias activas.

## Endpoints

Base path: `/api/implements`

- `POST /api/implements` (crear implemento; `category_id` opcional)
- `PUT /api/implements/{id}` (editar implemento; `category_id` opcional)
- `GET /api/implements/{id}` (obtener implemento; soporte para HU-14)

## Validaciones relevantes (PSD)

- Si `category_id` viene en el request, debe existir y estar activa.
- Si `category_id` es `null` o ausente, se persiste como `NULL` (permitido).

La validacion se reutiliza desde `CategoriaService.validarCategoriaActivaParaImplemento(...)`.

