- Estado del documento: histórico-vigente
- Última verificación: 2026-05-16
- Fuente de verdad: `V11__uuid_only_auth_audit_cleanup.sql`

# V11__uuid_only_auth_audit_cleanup.sql

## Motivo
Migrar relaciones de auditoría y revocación de tokens a UUID, luego eliminar referencias numéricas.

## Justificación
Evita que logs y revocaciones queden ligados a PKs en migración final.

## Cómo se llegó a esta conclusión
- Al migrar a UUID era obligatorio que tablas de auditoría compartieran el nuevo identificador.
- Se detectó necesidad de mantener trazabilidad de actor/target y usuario revocado por UUID.

## Impacto operativo
- **Módulos:** `auth`, `users`.
- **Endpoints `v2`:** login/logout y acciones de administración que generan eventos de audit.

## Estado
Histórico de transición.
