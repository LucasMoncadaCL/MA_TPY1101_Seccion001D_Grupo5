# Modulo: catalog/category

## Responsabilidad

Gestionar el ciclo de vida de categorias de implementos (crear, listar, editar, desactivar, eliminar) y exponer validaciones necesarias para integracion con inventario/implementos.

## Casos de uso implementados

- HU-74: gestion de categorias
  - listar para gestion
  - listar para selector
  - crear categoria
  - editar categoria
  - desactivar categoria (con `force`)
  - eliminar categoria
  - validar asociaciones y asignacion de implementos

## Fronteras

- Depende de:
  - `shared.error` (excepciones de negocio y respuesta uniforme)
  - `jooq` generado (`com.panol_project.backendpanol.jooq`)
- Expone:
  - API REST bajo `/api/categorias`
- Eventos:
  - No expone eventos de dominio aun
  - No escucha eventos aun

## Estructura interna

Ubicacion:

- `src/main/java/com/panol_project/backendpanol/modules/catalog/category`

Capas:

- `api/CategoriaController.java`
- `application/CategoriaService.java`
- `domain/*` (request/response y contratos de entrada/salida actuales)
- `infrastructure/CategoriaRepository.java`

## Decisiones tecnicas relevantes

1. Persistencia con jOOQ en `infrastructure`.
2. Validacion de nombre unico:
   - verificacion previa case-insensitive
   - fallback por manejo de unique violation SQLSTATE `23505`
3. Desactivacion con guardas por implementos activos asociados (`force` opcional).
4. Eliminacion bloqueada con implementos asociados.
5. Respuesta de error uniforme via `GlobalExceptionHandler`.

## Flujo principal (crear categoria)

```text
HTTP POST /api/categorias
  -> CategoriaController.crear()
    -> CategoriaService.crear(nombre)
      -> normalize + validateNombreUnico()
      -> CategoriaRepository.create()
        -> INSERT category (jOOQ)
      -> CategoriaResponse
```

## Riesgos/pendientes de arquitectura

1. `domain/` contiene DTOs de API; a futuro conviene separar:
   - dominio puro
   - DTOs de transporte (`api/dto` o similar)
2. Falta publicar eventos de dominio para desacoplar futuras integraciones con otros modulos.
