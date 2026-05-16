# Modulo: users

- Estado del documento: vigente
- Ultima verificacion: 2026-05-15
- Fuente de verdad: `UserAdminV2Controller`, `UserAdminService`, `ArchitectureTest`

## Responsabilidad

Administracion de usuarios (alta, actualizacion, cambio de rol, activacion/desactivacion y eliminacion).

## API vigente

Base path: `/api/v2/users`

- `GET /`
- `POST /`
- `PUT /{userUuid}`
- `PUT /{userUuid}/role`
- `PATCH /{userUuid}/active`
- `DELETE /{userUuid}`

## Fronteras

- Auditoria por `auth.domain.AuditLogPort` (excepcion documentada y permitida).
- Sin dependencias a API de otros modulos.
- Eventos de cambios de usuarios via outbox.

