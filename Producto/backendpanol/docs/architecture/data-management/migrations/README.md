- Estado del documento: vigente
- Última verificación: 2026-05-16
- Fuente de verdad: `backendpanol/src/main/resources/db/migration/*.sql` y validación funcional de endpoints `v2`

# Migraciones de Base de Datos: Registro de decisión y trazabilidad

Este directorio contiene una ficha técnica por cada script de Flyway bajo `db/migration`, con:

- Motivo de negocio/técnico de la migración.
- Evidencia de cómo se concluyó la necesidad.
- Impacto y riesgo operativos.
- Relación con cambios de API/endpoint (`v2`) más recientes.

## Índice de migraciones

- [V1__baseline.md](V1__baseline.md)
- [V2__locations_and_implement_location_fk.md](V2__locations_and_implement_location_fk.md)
- [V3__implement_observations_column.md](V3__implement_observations_column.md)
- [V4__v_stock_summary_display_location.md](V4__v_stock_summary_display_location.md)
- [V5__location_active_soft_delete.md](V5__location_active_soft_delete.md)
- [V7__auth_lock_revocation_audit.md](V7__auth_lock_revocation_audit.md)
- [V8__uuid_phase_a_base_columns.md](V8__uuid_phase_a_base_columns.md)
- [V9__uuid_phase_b_relationships_and_checks.md](V9__uuid_phase_b_relationships_and_checks.md)
- [V10__uuid_phase_c_sync_triggers.md](V10__uuid_phase_c_sync_triggers.md)
- [V11__uuid_only_auth_audit_cleanup.md](V11__uuid_only_auth_audit_cleanup.md)
- [V12__uuid_only_big_bang_cutover.md](V12__uuid_only_big_bang_cutover.md)
- [V13__uuid_only_post_checks.md](V13__uuid_only_post_checks.md)
- [V14__uuid_only_drop_legacy_ids.md](V14__uuid_only_drop_legacy_ids.md)
- [V15__uuid_only_cleanup_legacy_triggers.md](V15__uuid_only_cleanup_legacy_triggers.md)
- [V16__uuid_only_enforcement_checks.md](V16__uuid_only_enforcement_checks.md)
- [V17__uuid_only_auth_audit_and_postcheck_alignment.md](V17__uuid_only_auth_audit_and_postcheck_alignment.md)
- [V18__uuid_only_fix_implement_stock_trigger.md](V18__uuid_only_fix_implement_stock_trigger.md)
- [V19__stock_implement_uuid_unique_index.md](V19__stock_implement_uuid_unique_index.md)
- [V20__outbox_events.md](V20__outbox_events.md)
- [V21__auth_uuid_and_role_seed_alignment.md](V21__auth_uuid_and_role_seed_alignment.md)
- [V22__user_email_nullable.md](V22__user_email_nullable.md)
- [V23__user_email_mandatory_again.md](V23__user_email_mandatory_again.md)

## Convención usada en estas fichas

- `Motivo`: por qué nace la migración.
- `Justificación`: por qué era necesario para resolver un hallazgo real.
- `Cómo se llegó`: evidencia objetiva (checks, incidentes detectados, flujo de código dependiente).
- `Impacto`: módulos/endpoints afectados.
- `Estado actual`: si permanece vigente o fue parte de un corte de transición.
- `Observabilidad`: consultas de control recomendadas.

## Trazabilidad con el último cambio de endpoints (2026-05-16)

- Ajustes funcionales de estabilidad del endpoint `POST /api/v2/implements`:
  - Migraciones: `V18__uuid_only_fix_implement_stock_trigger.md`, `V19__stock_implement_uuid_unique_index.md`.
- Ajustes de soporte de contratos UUID / seguridad y roles:
  - Migraciones: `V8..V12`, `V14`, `V17`, `V21`.
- Soporte de auditoría/eventos y outbox:
  - Migraciones: `V7`, `V20`, `V11`, `V17`.
- Contratos de correo de usuario (mitigación y retorno de obligatoriedad):
  - Migraciones: `V22`, `V23`.

La fecha de revisión está fijada a `2026-05-16`; si se modifica el contrato HTTP o flujo de dominio, actualizar estas fichas en un mismo PR para mantener trazabilidad.
