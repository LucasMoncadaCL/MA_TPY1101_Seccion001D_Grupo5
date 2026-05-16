- Estado del documento: vigente
- Última verificación: 2026-05-16
- Fuente de verdad: `V20__outbox_events.sql`

# V20__outbox_events.sql

## Motivo
Crear tabla `outbox_events` para patrón de publicación confiable de eventos.

## Justificación
Centralizar integración asíncrona de eventos de dominio (audit/eventos secundarios) con reintentos y observabilidad.

## Cómo se llegó a esta conclusión
- Requerimientos de integración y auditoría mostraban necesidad de desacoplar transacciones HTTP de side-effects.
- Se definió estado de publicación (`PENDING/PROCESSED/FAILED`) y control de reintentos.

## Impacto operativo
- **Módulos:** `auth`, `users`, `catalog.stock` (según operación implementada).
- **Endpoints `v2`:** no cambia contratos HTTP; habilita comportamiento eventual robusto en cambios de usuario/movimientos.

## Estado
Vigente.

## Alineación con cambios recientes
Consolidado tras últimas pruebas de endpoints para evitar pérdidas de eventos y para registrar fallos de integración.
