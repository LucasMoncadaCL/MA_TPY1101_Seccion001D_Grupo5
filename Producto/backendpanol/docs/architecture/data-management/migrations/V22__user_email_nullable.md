- Estado del documento: histórica y reemplazada
- Última verificación: 2026-05-16
- Fuente de verdad: `V22__user_email_nullable.sql`

# V22__user_email_nullable.md

## Motivo
Permitir temporalmente `email` nulo para evitar bloqueo de escrituras en ciertos datos.

## Justificación
Respuesta de contingencia para escenarios de migración en que faltaban emails válidos.

## Cómo se llegó a esta conclusión
- Se detectó intento de inserción/actualización que no podía completar por restricción rígida de NOT NULL.
- Se usó cambio transitorio para evitar bloqueo operativo.

## Impacto operativo
- **Módulos:** `users`.
- **Endpoints `v2`:** administración de usuarios con datos incompletos.

## Estado
**No recomendada como estado final**.

## Alineación con endpoint reciente
Esta decisión fue compensada enseguida por `V23` para recuperar la obligatoriedad.
