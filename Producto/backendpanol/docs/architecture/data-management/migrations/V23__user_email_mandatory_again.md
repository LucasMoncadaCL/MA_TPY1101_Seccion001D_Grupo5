- Estado del documento: vigente (estado final de regla)
- Última verificación: 2026-05-16
- Fuente de verdad: `V23__user_email_mandatory_again.sql`

# V23__user_email_mandatory_again.sql

## Motivo
Reafirmar `email` obligatorio en `user` restaurando `NOT NULL` tras la ventana de mitigación.

## Justificación
El sistema de administración y validaciones de front esperan correo presente; permitir null reintroducía ambigüedad.

## Cómo se llegó a esta conclusión
- Se verificó que `V22` era temporal y rompía reglas de negocio de identificación.
- Se eligió backfill con placeholder (`pending+<uuid>@panol.local`) para conservar integridad histórica y luego exigir NOT NULL.

## Impacto operativo
- **Módulos:** `users`, validaciones de auth/admin.
- **Endpoints `v2`:** `POST /api/v2/users`, `PUT /api/v2/users/{uuid}`.

## Estado
Vigente y recomendado como estado final de dominio.
