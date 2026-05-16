- Estado del documento: vigente
- Última verificación: 2026-05-16
- Fuente de verdad: `V4__v_stock_summary_display_location.sql`

# V4__v_stock_summary_display_location.sql

## Motivo
Actualizar vista de resumen de stock para incluir datos de nombre (`category`, `location`) y estado visual de ubicación.

## Justificación
El front requiere un resumen amigable para stock y listados sin consultar múltiples joins por petición.

## Cómo se llegó a esta conclusión
- Se identificó la necesidad de consolidar `location/category` en una vista de lectura.
- Se estandarizó presentación de `display_location` (`Prestado` vs nombre de bodega).

## Impacto operativo
- **Módulos:** `catalog.stock`, `catalog.implement`.
- **Endpoints `v2`:** listados de stock/implementos consumen esta estructura o la asimilan por equivalencia.

## Estado
Vigente como base de consulta, luego fue migrada a UUID en fases posteriores.

## Notas de evolución
Esta vista fue reemplazada por versión UUID en migración `V14` para conservar compatibilidad.
