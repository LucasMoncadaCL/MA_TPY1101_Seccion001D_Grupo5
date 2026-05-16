- Estado del documento: vigente
- Última verificación: 2026-05-16
- Fuente de verdad: `V1__baseline.sql`

# V1__baseline.sql

## Motivo
Evitar recrear esquema completo en entornos con base existente (Supabase pre-cargada).

## Justificación
La base de datos destino ya estaba inicializada; se define `baseline` para que Flyway registre el estado inicial y sólo aplique migraciones incrementales.

## Cómo se llegó a esta conclusión
- Se detectó en ambiente real que ya existía el esquema canónico.
- El mismo enfoque de `baseline-on-migrate` reduce riesgo de `ddl` repetido y pérdida de estado.

## Impacto
- No aplica cambios físicos nuevos.
- Define punto de partida para todas las migraciones `V2+`.

## Estado
Vigente como contrato de arranque con entornos legacy.

## Relación con últimos cambios de endpoints
No altera contratos HTTP, pero habilita que la evolución posterior (especialmente UUID + outbox) se aplique sólo una vez sobre el mismo punto de partida.
