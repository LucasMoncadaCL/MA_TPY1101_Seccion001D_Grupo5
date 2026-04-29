# PSD-109 - Validación de nombre único al editar producto (Backend)

Fecha: 2026-04-29

## Resumen

Se implementó la validación de **nombre único** al editar un implemento (`PUT /api/implements/{id}`), verificando que no exista **otro implemento activo** con el mismo nombre (case-insensitive) **excluyendo el registro actual** (`id != :id`).

Si hay duplicado, la API responde `400 Bad Request` con:

- `code`: `IMPLEMENT_NAME_DUPLICATE`
- `message`: `Ya existe un producto con el nombre '{nombre}'`

## Alcance funcional

### Regla de negocio

Al editar:

- Se normaliza el nombre (trim).
- Se valida que **no exista** otro implemento activo con el mismo nombre (case-insensitive), excluyendo el `id` en edición.

SQL equivalente (referencial):

```sql
WHERE LOWER(name) = LOWER(:name)
  AND id != :id
  AND active = true
```

## Cambios técnicos

### 1) Repositorio (contrato)

Se usa un método dedicado para update que excluye el `id` actual:

- `src/main/java/com/panol_project/backendpanol/modules/catalog/implement/domain/ImplementRepository.java`
  - `existsActiveByNameIgnoreCaseAndIdNot(String nombre, Integer excludedId)`

### 2) Repositorio (implementación jOOQ)

En `ImplementJooqRepository` se implementa la existencia con:

- filtro `IMPLEMENT.ACTIVE = true`
- exclusión `IMPLEMENT.ID <> excludedId`
- comparación case-insensitive exacta con `IMPLEMENT.NAME.likeIgnoreCase(nombre)`

Archivo:

- `src/main/java/com/panol_project/backendpanol/modules/catalog/implement/infrastructure/ImplementJooqRepository.java`

### 3) Servicio de aplicación

En `ImplementService` la regla se aplica antes de persistir, y se mantiene un fallback por constraint único (`SQLState=23505`):

- `validateUniqueActiveNameForUpdate(normalizedName, id)`
- `duplicateNameException(...)` centraliza el mensaje/código

Archivo:

- `src/main/java/com/panol_project/backendpanol/modules/catalog/implement/application/ImplementService.java`

## Pruebas

Se valida el caso de duplicado en edición (excluyendo el propio registro) con test unitario:

- `src/test/java/com/panol_project/backendpanol/modules/catalog/implement/application/ImplementServiceTest.java`
  - `editarDebeFallarConBadRequestSiNombreActivoExisteEnOtroImplemento`

## Cómo validar manualmente (sin JWT)

1) Crear (o identificar) dos implementos activos con nombres distintos (ej: `A` e `B`).
2) Editar `A` intentando usar el `name` de `B`.
3) Resultado esperado: `400` con `IMPLEMENT_NAME_DUPLICATE`.

Ejemplo (PowerShell):

```powershell
Invoke-RestMethod -Method Put `
  -Uri "http://localhost:18080/api/implements/<ID_A>" `
  -ContentType "application/json" `
  -Body (@{
    name = "<NOMBRE_DE_B>"
    description = $null
    category_id = 2
    location_id = 1
    item_type = "reusable"
    min_stock = 1
    observations = $null
  } | ConvertTo-Json)
```

## Notas

- Esta validación es distinta a la de creación, ya que **debe excluir** el registro en edición (no se reutiliza el mismo método).
- La regla se aplica solo sobre implementos **activos**.

