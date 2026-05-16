- Estado del documento: vigente en contexto actual
- Última verificación: 2026-05-16
- Fuente de verdad: `V18__uuid_only_fix_implement_stock_trigger.sql`

# V18__uuid_only_fix_implement_stock_trigger.sql

## Motivo
Reparar trigger de sincronización `implement -> stock` para usar `implement_uuid`.

## Justificación
Tras el corte UUID, quedó trigger legado escribiendo con `implement_id`, provocando errores de inserción y de referencia.

## Cómo se llegó a esta conclusión
- Se detectó sintomáticamente al crear implementos: fallas por referencias a columnas legacy en flujo de stock.
- Se inspeccionaron funciones/ triggers `public` con `implement_id` y lógica sobre `stock`.

## Impacto operativo
- **Módulos:** `catalog.implement`, `catalog.stock`.
- **Endpoints `v2` impactados:**
  - `POST /api/v2/implements`
  - `POST /api/v2/stock` y operaciones que dependen de stock inicial.
- Es una migración crítica para evitar 500 tras creación.

## Estado
Vigente.
