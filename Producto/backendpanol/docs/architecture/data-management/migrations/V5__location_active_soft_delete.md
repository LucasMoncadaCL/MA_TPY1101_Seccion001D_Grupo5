- Estado del documento: vigente
- Última verificación: 2026-05-16
- Fuente de verdad: `V5__location_active_soft_delete.sql`

# V5__location_active_soft_delete.md

## Motivo
Agregar soft delete lógico en `location` (`active`) e índice de soporte.

## Justificación
Evitar borrado físico de ubicaciones históricas y permitir controles de activación/desactivación.

## Cómo se llegó a esta conclusión
- Requerimiento de mantener referencias históricas (historial de movimientos/stock).
- Necesidad de filtrar catálogos activos sin perder trazabilidad.

## Impacto operativo
- **Módulos:** `catalog.location`.
- **Endpoints `v2`:** administración de ubicaciones (GET/PUT/PATCH).

## Estado
Vigente.
