# JWT Claims - Pañol Salud

- Última actualización: 2026-05-09
- Fuente: `AuthService` + configuración de seguridad backend

## Claims emitidos por `/api/v1/auth/login`

1. `iss` (string)
- Emisor del token.
- Configurable por `APP_AUTH_JWT_ISSUER`.
- Valor por defecto: `panol-backend`.

2. `sub` (string)
- Identificador principal del usuario autenticado.
- Corresponde al `uuid` canónico del usuario en PostgreSQL.

3. `user_id` (number)
- Identificador numérico del usuario autenticado.
- Claim legado temporal para compatibilidad durante migración dual-key.

4. `user_uuid` (string)
- Refuerzo explícito del identificador UUID del usuario.
- Debe coincidir con `sub`.

5. `role` (string)
- Rol normalizado del usuario.
- Valores esperados: `COORDINADOR`, `DIRECTOR`, `DOCENTE`.

6. `jti` (string)
- Identificador único del token (UUID).
- Se usa para revocación en logout.

7. `iat` (number, epoch seconds)
- Fecha/hora de emisión.

8. `exp` (number, epoch seconds)
- Fecha/hora de expiración.

## Header JWT

1. `alg`: `HS256`

## Notas

- El sistema actual usa firma simétrica `HS256`.
- Para estrategia de migración a `RS256`, revisar:
  - `docs/decisiones-arquitectura/ADR-002-plan-migracion-rs256.md`
