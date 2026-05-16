- Estado del documento: historico
- Ultima verificacion: 2026-05-15
- Fuente de verdad: ver matriz canonica vigente y codigo fuente actual

# PSD-110 - Endpoint PUT /api/implements/{id} (EdiciÃ³n producto) (Backend)

Fecha: 2026-04-29

## Resumen

Se implementÃ³ el endpoint de ediciÃ³n de implementos en Spring Boot:

- `PUT /api/implements/{id}`

Permite modificar:

- `name`
- `category_id`
- `item_type`
- `location_id`
- `description`
- `min_stock`
- `observations`

AdemÃ¡s:

- valida autenticaciÃ³n JWT y restringe acceso a rol `COORDINADOR`
- retorna `404` si el implemento no existe
- retorna `400` si el implemento estÃ¡ inactivo con mensaje **exacto**: `No se puede editar un producto inactivo`
- retorna `200` con el producto actualizado

## Contrato del endpoint

### Request

Body JSON (campos obligatorios / validaciones):

- `name`: requerido, mÃ¡ximo 120
- `description`: opcional, mÃ¡ximo 255
- `category_id`: opcional (se valida activa si se envÃ­a)
- `location_id`: requerido
- `item_type`: requerido, uno de `consumable|reusable|individual`
- `min_stock`: requerido, entero positivo
- `observations`: opcional, mÃ¡ximo 500

DTO:

- `src/main/java/com/panol_project/backendpanol/modules/catalog/implement/api/dto/UpdateImplementRequest.java`

### Response

`200 OK` con `ImplementResponse` (incluye `min_stock` y `observations`):

- `src/main/java/com/panol_project/backendpanol/modules/catalog/implement/api/dto/ImplementResponse.java`

## ImplementaciÃ³n

### 1) Controlador + seguridad

- Se agregÃ³ `@PreAuthorize("hasRole('COORDINADOR')")` al `PUT`.
- Se ampliÃ³ el handler de ediciÃ³n para enviar todos los campos al servicio.
- Se retorna `observations` desde el estado persistido.

Archivo:

- `src/main/java/com/panol_project/backendpanol/modules/catalog/implement/api/ImplementController.java`

### 2) Servicio de aplicaciÃ³n (reglas)

En `ImplementService.editar(...)`:

- `404`: si no existe (`requireImplement`)
- `400`: si estÃ¡ inactivo (`IMPLEMENT_INACTIVE`, mensaje: `No se puede editar un producto inactivo`)
- validaciÃ³n de categorÃ­a activa (si corresponde)
- validaciÃ³n de existencia de ubicaciÃ³n
- parse/validaciÃ³n de `item_type`
- validaciÃ³n de nombre Ãºnico en ediciÃ³n (solo activos, excluye `id`)
- persistencia de cambios en `implement`
- actualizaciÃ³n de `min_stock` en `stock` (solo `MIN_STOCK`)

Archivo:

- `src/main/java/com/panol_project/backendpanol/modules/catalog/implement/application/ImplementService.java`

### 3) Persistencia (jOOQ)

Se actualizan columnas en `implement`:

- `name`, `description`, `category_id`, `location_id`, `item_type`, `observations`
- `updated_at` se setea en cada update

Archivo:

- `src/main/java/com/panol_project/backendpanol/modules/catalog/implement/infrastructure/ImplementJooqRepository.java`

### 4) Dominio

Se extendiÃ³ el record `Implemento` para incluir `observations`.

Archivo:

- `src/main/java/com/panol_project/backendpanol/modules/catalog/implement/domain/Implemento.java`

## Sobre â€œno tocar stock / loan_detailâ€

- `loan_detail`: no se lee ni se escribe en el flujo de ediciÃ³n.
- `stock`: para poder editar `min_stock`, se actualiza **solo** `STOCK.MIN_STOCK` usando `updateMinStockByImplementId(...)` (no modifica cantidades `total/available/reserved/loaned/damaged`).

## Pruebas

### Unit tests (service)

- `src/test/java/com/panol_project/backendpanol/modules/catalog/implement/application/ImplementServiceTest.java`
  - ediciÃ³n exitosa actualiza `min_stock`
  - ediciÃ³n falla si implemento estÃ¡ inactivo (`IMPLEMENT_INACTIVE`)
  - ediciÃ³n falla por nombre duplicado (`IMPLEMENT_NAME_DUPLICATE`)

### WebMvc security tests

Se cubre seguridad del endpoint `PUT`:

- requiere JWT
- rechaza sin rol `COORDINADOR`
- permite con rol `COORDINADOR`

Archivo:

- `src/test/java/com/panol_project/backendpanol/api/ImplementCreateEndpointSecurityTest.java`

## CÃ³mo validar manualmente

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

### ValidaciÃ³n de request

Ejemplo de validaciÃ³n por `item_type` invÃ¡lido o `min_stock=0`:

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



## Advertencia historica

Este documento conserva contexto tecnico de una etapa anterior. No debe usarse como guia operativa primaria sin contrastar con la documentacion vigente.

## Estado actual (vigente)

- Contratos publicos: solo /api/v2/**.
- Seguridad: permitAll solo en POST /api/v2/auth/login (+ health/info).
- Eventos: outbox operativo con estados PENDING/PROCESSED/FAILED.
- Compose principal: Producto/docker-compose.yaml (frontend + backend, sin postgres local).

