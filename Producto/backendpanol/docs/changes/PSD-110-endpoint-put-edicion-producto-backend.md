# PSD-110 - Endpoint PUT /api/implements/{id} (Edición producto) (Backend)

Fecha: 2026-04-29

## Resumen

Se implementó el endpoint de edición de implementos en Spring Boot:

- `PUT /api/implements/{id}`

Permite modificar:

- `name`
- `category_id`
- `item_type`
- `location_id`
- `description`
- `min_stock`
- `observations`

Además:

- valida autenticación JWT y restringe acceso a rol `COORDINADOR`
- retorna `404` si el implemento no existe
- retorna `400` si el implemento está inactivo con mensaje **exacto**: `No se puede editar un producto inactivo`
- retorna `200` con el producto actualizado

## Contrato del endpoint

### Request

Body JSON (campos obligatorios / validaciones):

- `name`: requerido, máximo 120
- `description`: opcional, máximo 255
- `category_id`: opcional (se valida activa si se envía)
- `location_id`: requerido
- `item_type`: requerido, uno de `consumable|reusable|individual`
- `min_stock`: requerido, entero positivo
- `observations`: opcional, máximo 500

DTO:

- `src/main/java/com/panol_project/backendpanol/modules/catalog/implement/api/dto/UpdateImplementRequest.java`

### Response

`200 OK` con `ImplementResponse` (incluye `min_stock` y `observations`):

- `src/main/java/com/panol_project/backendpanol/modules/catalog/implement/api/dto/ImplementResponse.java`

## Implementación

### 1) Controlador + seguridad

- Se agregó `@PreAuthorize("hasRole('COORDINADOR')")` al `PUT`.
- Se amplió el handler de edición para enviar todos los campos al servicio.
- Se retorna `observations` desde el estado persistido.

Archivo:

- `src/main/java/com/panol_project/backendpanol/modules/catalog/implement/api/ImplementController.java`

### 2) Servicio de aplicación (reglas)

En `ImplementService.editar(...)`:

- `404`: si no existe (`requireImplement`)
- `400`: si está inactivo (`IMPLEMENT_INACTIVE`, mensaje: `No se puede editar un producto inactivo`)
- validación de categoría activa (si corresponde)
- validación de existencia de ubicación
- parse/validación de `item_type`
- validación de nombre único en edición (solo activos, excluye `id`)
- persistencia de cambios en `implement`
- actualización de `min_stock` en `stock` (solo `MIN_STOCK`)

Archivo:

- `src/main/java/com/panol_project/backendpanol/modules/catalog/implement/application/ImplementService.java`

### 3) Persistencia (jOOQ)

Se actualizan columnas en `implement`:

- `name`, `description`, `category_id`, `location_id`, `item_type`, `observations`
- `updated_at` se setea en cada update

Archivo:

- `src/main/java/com/panol_project/backendpanol/modules/catalog/implement/infrastructure/ImplementJooqRepository.java`

### 4) Dominio

Se extendió el record `Implemento` para incluir `observations`.

Archivo:

- `src/main/java/com/panol_project/backendpanol/modules/catalog/implement/domain/Implemento.java`

## Sobre “no tocar stock / loan_detail”

- `loan_detail`: no se lee ni se escribe en el flujo de edición.
- `stock`: para poder editar `min_stock`, se actualiza **solo** `STOCK.MIN_STOCK` usando `updateMinStockByImplementId(...)` (no modifica cantidades `total/available/reserved/loaned/damaged`).

## Pruebas

### Unit tests (service)

- `src/test/java/com/panol_project/backendpanol/modules/catalog/implement/application/ImplementServiceTest.java`
  - edición exitosa actualiza `min_stock`
  - edición falla si implemento está inactivo (`IMPLEMENT_INACTIVE`)
  - edición falla por nombre duplicado (`IMPLEMENT_NAME_DUPLICATE`)

### WebMvc security tests

Se cubre seguridad del endpoint `PUT`:

- requiere JWT
- rechaza sin rol `COORDINADOR`
- permite con rol `COORDINADOR`

Archivo:

- `src/test/java/com/panol_project/backendpanol/api/ImplementCreateEndpointSecurityTest.java`

## Cómo validar manualmente

### PUT exitoso (sin JWT, cuando `app.security.enabled=false`)

```powershell
Invoke-RestMethod -Method Put `
  -Uri "http://localhost:18080/api/implements/1" `
  -ContentType "application/json" `
  -Body (@{
    name = "Guantes latex (editado)"
    description = "Descripcion actualizada"
    category_id = 2
    location_id = 1
    item_type = "reusable"
    min_stock = 2
    observations = "Obs actualizadas"
  } | ConvertTo-Json)
```

### Validación de request

Ejemplo de validación por `item_type` inválido o `min_stock=0`:

```powershell
try {
  Invoke-RestMethod -Method Put `
    -Uri "http://localhost:18080/api/implements/1" `
    -ContentType "application/json" `
    -Body (@{
      name = "X"
      description = $null
      category_id = 2
      location_id = 1
      item_type = "INVALIDO"
      min_stock = 0
      observations = "Obs"
    } | ConvertTo-Json)
} catch {
  $_.Exception.Response.GetResponseStream() | %{
    $r = New-Object IO.StreamReader($_)
    $r.ReadToEnd()
  }
}
```

