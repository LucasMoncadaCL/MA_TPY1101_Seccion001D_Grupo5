- Estado del documento: vigente
- Última verificación: 2026-05-16
- Fuente de verdad: `V21__auth_uuid_and_role_seed_alignment.sql`

# V21__auth_uuid_and_role_seed_alignment.sql

## Motivo
Sincronizar `auth_uuid` de usuario con `uuid` y asegurar semillas de roles base.

## Justificación
Sin `auth_uuid` y roles canónicos (`DIRECTOR`, `COORDINADOR`, `DOCENTE`) había riesgo de fallas de login/rol en seeds y validaciones.

## Cómo se llegó a esta conclusión
- Error de creación/administración de usuarios por inconsistencias de role UUID / UUID ausente en `user`.
- Se incorporó inserción idempotente de roles con fallback por esquema legacy o UUID.

## Impacto operativo
- **Módulos:** `auth`, `users`, `catalog` (por permisos de rol).
- **Endpoints `v2`:** login, create-user, endpoints de administración de usuarios.

## Estado
Vigente.
