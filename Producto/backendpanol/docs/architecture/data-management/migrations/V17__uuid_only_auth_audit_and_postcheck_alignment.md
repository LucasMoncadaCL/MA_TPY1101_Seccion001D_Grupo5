- Estado del documento: histórico-vigente
- Última verificación: 2026-05-16
- Fuente de verdad: `V17__uuid_only_auth_audit_and_postcheck_alignment.sql`

# V17__uuid_only_auth_audit_and_postcheck_alignment.sql

## Motivo
Estabilizar UUID en tablas auth/audit y alinear vistas de validación final.

## Justificación
El corte UUID quedó incompleto en algunas tablas de control; se cerró en bloque independiente para mantener reversibilidad de checksums.

## Cómo se llegó a esta conclusión
- Revisión de estado `V14` + uso de módulos de autenticación mostró necesidad de `uuid` propios en `audit_log` y `token_revocation`.

## Impacto operativo
- **Módulos:** `auth`.
- **Endpoints `v2`:** robustez en auditoría y revocación durante login/logout y cambios de usuario.

## Estado
Histórico de estabilización.
