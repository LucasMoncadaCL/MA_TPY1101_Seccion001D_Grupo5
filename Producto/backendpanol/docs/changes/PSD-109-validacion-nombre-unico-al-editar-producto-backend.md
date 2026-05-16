- Estado del documento: historico
- Ultima verificacion: 2026-05-15
- Fuente de verdad: ver matriz canonica vigente y codigo fuente actual

# PSD-109 - ValidaciÃ³n de nombre Ãºnico al editar producto (Backend)

Fecha: 2026-04-29

## Resumen

Se implementÃ³ la validaciÃ³n de **nombre Ãºnico** al editar un implemento (`PUT /api/implements/{id}`), verificando que no exista **otro implemento activo** con el mismo nombre (case-insensitive) **excluyendo el registro actual** (`id != :id`).

Si hay duplicado, la API responde `400 Bad Request` con:

- `code`: `IMPLEMENT_NAME_DUPLICATE`
- `message`: `Ya existe un producto con el nombre '{nombre}'`

## Alcance funcional

### Regla de negocio

Al editar:

- Se normaliza el nombre (trim).
- Se valida que **no exista** otro implemento activo con el mismo nombre (case-insensitive), excluyendo el `id` en ediciÃ³n.

SQL equivalente (referencial):

```sql
WHERE LOWER(name) = LOWER(:name)
  AND id != :id
  AND active = true
```

## Cambios tÃ©cnicos

### 1) Repositorio (contrato)

Se usa un mÃ©todo dedicado para update que excluye el `id` actual:

- `src/main/java/com/panol_project/backendpanol/modules/catalog/implement/domain/ImplementRepository.java`
  - `existsActiveByNameIgnoreCaseAndIdNot(String nombre, Integer excludedId)`

### 2) Repositorio (implementaciÃ³n jOOQ)

En `ImplementJooqRepository` se implementa la existencia con:

- filtro `IMPLEMENT.ACTIVE = true`
- exclusiÃ³n `IMPLEMENT.ID <> excludedId`
- comparaciÃ³n case-insensitive exacta con `IMPLEMENT.NAME.likeIgnoreCase(nombre)`

Archivo:

- `src/main/java/com/panol_project/backendpanol/modules/catalog/implement/infrastructure/ImplementJooqRepository.java`

### 3) Servicio de aplicaciÃ³n

En `ImplementService` la regla se aplica antes de persistir, y se mantiene un fallback por constraint Ãºnico (`SQLState=23505`):

- `validateUniqueActiveNameForUpdate(normalizedName, id)`
- `duplicateNameException(...)` centraliza el mensaje/cÃ³digo

Archivo:

- `src/main/java/com/panol_project/backendpanol/modules/catalog/implement/application/ImplementService.java`

## Pruebas

Se valida el caso de duplicado en ediciÃ³n (excluyendo el propio registro) con test unitario:

- `src/test/java/com/panol_project/backendpanol/modules/catalog/implement/application/ImplementServiceTest.java`
  - `editarDebeFallarConBadRequestSiNombreActivoExisteEnOtroImplemento`

## CÃ³mo validar manualmente (sin JWT)

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

- Esta validaciÃ³n es distinta a la de creaciÃ³n, ya que **debe excluir** el registro en ediciÃ³n (no se reutiliza el mismo mÃ©todo).
- La regla se aplica solo sobre implementos **activos**.



## Advertencia historica

Este documento conserva contexto tecnico de una etapa anterior. No debe usarse como guia operativa primaria sin contrastar con la documentacion vigente.

## Estado actual (vigente)

- Contratos publicos: solo /api/v2/**.
- Seguridad: permitAll solo en POST /api/v2/auth/login (+ health/info).
- Eventos: outbox operativo con estados PENDING/PROCESSED/FAILED.
- Compose principal: Producto/docker-compose.yaml (frontend + backend, sin postgres local).

