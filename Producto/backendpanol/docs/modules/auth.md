# Modulo: auth

- Estado del documento: vigente
- Ultima verificacion: 2026-05-15
- Fuente de verdad: `AuthV2Controller`, `AuthService`, `AuditLogPort`

## Responsabilidad

Autenticacion con JWT, bloqueo por intentos fallidos y revocacion de token en logout.

## API vigente

Base path: `/api/v2/auth`

- `POST /login`
- `POST /logout`

## Reglas clave

- Login es el unico endpoint auth con `permitAll`.
- Logout requiere JWT valido.
- Auditoria via puerto de dominio (`AuditLogPort`).
- Eventos de auth via outbox cuando aplica.

