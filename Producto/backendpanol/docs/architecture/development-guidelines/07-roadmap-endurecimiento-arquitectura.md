# 07 - Roadmap de Endurecimiento Arquitectónico

## Horizonte 0-30 días

1. Congelar deuda nueva:
- No nuevos servicios `application` con `DSLContext` directo.

2. Trazabilidad mínima:
- estandarizar eventos de auditoría.

3. Checklist PR obligatorio.

## Horizonte 30-60 días

1. Extraer puertos en casos críticos (`users`, `auth`, `loan`).
2. Unificar naming de repositorios/adaptadores.
3. Crear tests de integración por adapter.

## Horizonte 60-90 días

1. Introducir tabla `outbox_events`.
2. Worker de publicación a Mongo.
3. Métricas de retries y eventos pendientes.

## Horizonte 90+ días

1. Revisar límites de dominios (separar `loan` explícito si aún está mezclado).
2. Evaluar modularización adicional por bounded context.
3. ADRs formales para decisiones estructurales mayores.

## KPIs de seguimiento

- % casos de uso con puertos (sin acoplamiento técnico).
- % PRs con checklist completo.
- p95 endpoints críticos.
- tasa de fallas de publicación de eventos.
