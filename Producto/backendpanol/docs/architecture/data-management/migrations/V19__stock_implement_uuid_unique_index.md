- Estado del documento: vigente en contexto actual
- Última verificación: 2026-05-16
- Fuente de verdad: `V19__stock_implement_uuid_unique_index.sql`

# V19__stock_implement_uuid_unique_index.sql

## Motivo
Garantizar unicidad de `stock.implement_uuid` para soportar `ON CONFLICT` y eliminar duplicados existentes.

## Justificación
Sin índice único, `insert .. on conflict (implement_uuid)` fallaba en runtime y forzaba errores 500 en alta de implementos.

## Cómo se llegó a esta conclusión
- Durante smoke de `POST /api/v2/implements` se observaron inconsistencias por duplicados de `implement_uuid`.
- El plan de migración requiere deduplicación antes de crear índice único.

## Impacto operativo
- **Módulos:** `catalog.implement`, `catalog.stock`.
- **Endpoints `v2` impactados:** especialmente `POST /api/v2/implements` (resiliencia a inserciones repetidas/estado previo).

## Estado
Vigente y directamente alineado con correcciones de estabilidad funcional.
