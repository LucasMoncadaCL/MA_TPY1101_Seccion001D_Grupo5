- Estado del documento: histórico-vigente
- Última verificación: 2026-05-16
- Fuente de verdad: `V9__uuid_phase_b_relationships_and_checks.sql`

# V9__uuid_phase_b_relationships_and_checks.sql

## Motivo
Agregar columnas UUID de relación y migrar backfill entre FK legadas y UUID.

## Justificación
Permitir transición sin downtime: tabla puede tener `id` y `uuid` mientras se migra integridad referencial.

## Cómo se llegó a esta conclusión
- Se necesitó conservar compatibilidad temporal para datos existentes.
- Se detectó que varias relaciones podían perderse durante migración si no se backfleaban explícitamente.

## Impacto operativo
- **Módulos:** casi todo el dominio catálogo/usuarios/stock/auth.
- **Endpoints `v2`:** estabilidad para lectura y escritura de relaciones durante transición.

## Estado
Vigente como fase de migración (pasado).

## Relación de endpoints
Soporta el paso de módulos `v2` a UUID sin romper solicitudes de categorías, ubicaciones, implementos, usuarios y movimientos.
