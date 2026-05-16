- Estado del documento: histórico-vigente
- Última verificación: 2026-05-16
- Fuente de verdad: `V12__uuid_only_big_bang_cutover.sql`

# V12__uuid_only_big_bang_cutover.sql

## Motivo
Aplicar rellenado masivo de UUID y unicidad por tabla como preparación de cierre.

## Justificación
Garantizar que ningún registro quede sin UUID antes de retirar llaves `id`.

## Cómo se llegó a esta conclusión
- Migración fase-orientada: solo cortar a UUID si hay integridad completa.
- Se usa lógica idempotente para no romper entornos con distintos estados.

## Impacto operativo
- **Módulos:** todos los módulos con PK.
- **Endpoints `v2`:** habilita consumos UUID consistentes.

## Estado
Histórico de transición.
