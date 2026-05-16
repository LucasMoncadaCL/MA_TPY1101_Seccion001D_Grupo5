- Estado del documento: vigente
- Última verificación: 2026-05-16
- Fuente de verdad: `V7__auth_lock_revocation_audit.sql`

# V7__auth_lock_revocation_audit.sql

## Motivo
Agregar soporte de revocación de JWT y auditoría básica de seguridad.

## Justificación
Necesario para endurecer autenticación: bloqueo por intentos fallidos y revocación explícita por logout.

## Cómo se llegó a esta conclusión
- Operación real de auth requería trazabilidad de cambios de sesión.
- Se detectaron casos de acceso inválido que debían quedar registrados.

## Impacto operativo
- **Módulos:** `auth`, `users`.
- **Endpoints `v2`:** `POST /api/v2/auth/login`, `POST /api/v2/auth/logout`, creación/edición de usuarios con trazabilidad.

## Estado
Vigente (evolucionado por migraciones UUID posteriores).
