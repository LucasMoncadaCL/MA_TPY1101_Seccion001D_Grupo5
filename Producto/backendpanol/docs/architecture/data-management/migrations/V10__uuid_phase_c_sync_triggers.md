- Estado del documento: histórico-vigente
- Última verificación: 2026-05-16
- Fuente de verdad: `V10__uuid_phase_c_sync_triggers.sql`

# V10__uuid_phase_c_sync_triggers.sql

## Motivo
Añadir sincronización automática entre pares `id`/`uuid` y validar consistencia parcial.

## Justificación
Durante corte gradual se requieren datos coherentes aún cuando llega info en uno u otro formato.

## Cómo se llegó a esta conclusión
- Se observó coexistencia de identificadores en inserts y updates.
- Se creó lógica de trigger para prevenir estados inconsistentes y `auth_uuid` vacío.

## Impacto operativo
- **Módulos:** `users`, `loan`, `implement`, `stock`.
- **Endpoints `v2`:** evita errores de integridad en escrituras mientras backend migra consumidores.

## Estado
Vigente como parte de transición.

## Riesgo
Debe desaparecer después del corte final para no generar lógica redundante.
