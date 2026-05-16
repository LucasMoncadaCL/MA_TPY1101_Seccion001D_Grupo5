- Estado del documento: histórico-vigente
- Última verificación: 2026-05-16
- Fuente de verdad: `V14__uuid_only_drop_legacy_ids.sql`

# V14__uuid_only_drop_legacy_ids.sql

## Motivo
Eliminar columnas e identificadores legacy (`id`, `*_id`) donde existe `uuid`, y rearmar vistas con joins UUID.

## Justificación
Completa el corte hacia UUID-only para eliminar ambigüedad y errores de mapeo en repos/jOOQ.

## Cómo se llegó a esta conclusión
- Se observaron fallos recurrentes por ambigüedad entre `id` y `uuid` en el runtime de consultas.
- Esta migración cierra dependencia legacy y rehace vista crítica de stock.

## Impacto operativo
- **Módulos:** transversal, especialmente `catalog.implement`, `catalog.stock`, `catalog.category`.
- **Endpoints `v2`:** base necesaria para eliminar errores como `column category.id does not exist` en implementes.

## Estado
Histórico, pero fundamental para comportamiento actual.
