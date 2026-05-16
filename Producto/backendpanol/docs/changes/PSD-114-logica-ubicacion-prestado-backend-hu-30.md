- Estado del documento: historico
- Ultima verificacion: 2026-05-15
- Fuente de verdad: ver matriz canonica vigente y codigo fuente actual

# PSD-114 â€” LÃ³gica de ubicaciÃ³n automÃ¡tica â€œPrestadoâ€ (Backend) â€” HU-30

Fecha: 2026-04-29

## Objetivo

Aplicar una lÃ³gica consistente (en backend) para exponer la ubicaciÃ³n â€œvisibleâ€ del producto:

- Si `stock.loaned > 0`, el campo `display_location` debe ser `"Prestado"`.
- Si `stock.loaned = 0`, `display_location` debe ser el nombre real de la ubicaciÃ³n.

La lÃ³gica se implementa en el backend (Service / DB view) para asegurar consistencia para cualquier consumidor de la API.

## ImplementaciÃ³n

### API: `GET /api/implements/{id}`

Se agrega el campo `display_location` en la respuesta del endpoint.

Regla:

- `loaned > 0` â‡’ `"Prestado"`
- en caso contrario â‡’ `location.name`

Archivos:

- `Producto/backendpanol/src/main/java/com/panol_project/backendpanol/modules/catalog/implement/application/ImplementService.java` (`resolveDisplayLocation`)
- `Producto/backendpanol/src/main/java/com/panol_project/backendpanol/modules/catalog/implement/api/ImplementController.java` (mapea `display_location`)
- `Producto/backendpanol/src/main/java/com/panol_project/backendpanol/modules/catalog/implement/api/dto/ImplementResponse.java` (nuevo campo `display_location`)

### DB: `v_stock_summary`

Se versiona la vista con Flyway agregando la columna `display_location` con la misma regla.

Archivo:

- `Producto/backendpanol/src/main/resources/db/migration/V4__v_stock_summary_display_location.sql`

## Contrato esperado (API)

Ejemplo:

```json
{
  "location": { "id": 1, "name": "Por definir" },
  "display_location": "Prestado"
}
```

## CÃ³mo probar

1) Asegura que el implemento tenga `loaned > 0` en `stock`.
2) Consulta:

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:18080/api/implements/1"
```

Resultado esperado:

- Con `loaned > 0` â‡’ `display_location = "Prestado"`.
- Con `loaned = 0` â‡’ `display_location = location.name`.



## Advertencia historica

Este documento conserva contexto tecnico de una etapa anterior. No debe usarse como guia operativa primaria sin contrastar con la documentacion vigente.

## Estado actual (vigente)

- Contratos publicos: solo /api/v2/**.
- Seguridad: permitAll solo en POST /api/v2/auth/login (+ health/info).
- Eventos: outbox operativo con estados PENDING/PROCESSED/FAILED.
- Compose principal: Producto/docker-compose.yaml (frontend + backend, sin postgres local).

