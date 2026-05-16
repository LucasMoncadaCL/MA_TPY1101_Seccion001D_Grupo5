- Estado del documento: histórico-vigente
- Última verificación: 2026-05-16
- Fuente de verdad: `V15__uuid_only_cleanup_legacy_triggers.sql`

# V15__uuid_only_cleanup_legacy_triggers.md

## Motivo
Remover triggers/funiones duales y mantener solo sincronización UUID en usuarios.

## Justificación
Evitar doble-fuente de verdad entre `_id` y `uuid` luego de fases de migración.

## Cómo se llegó a esta conclusión
- Quedaban objetos SQL del periodo mixto que podían interferir con writes futuros.
- Se simplificó lógica para `auth_uuid` exclusivamente.

## Impacto operativo
- **Módulos:** `users`.
- **Endpoints `v2`:** mantenimiento de estabilidad al guardar usuarios.

## Estado
Histórico.
