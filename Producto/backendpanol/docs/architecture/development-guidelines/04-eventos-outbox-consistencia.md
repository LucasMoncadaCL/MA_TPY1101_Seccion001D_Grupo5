# 04 - Eventos, Outbox y Consistencia

## Propósito

Tener eventos confiables sin comprometer consistencia transaccional.

## Regla actual

- SQL primero (commit), luego publicación/registro de evento.

## Regla objetivo

- Patrón Outbox:
  1. Caso de uso escribe estado SQL + evento outbox en misma transacción.
  2. Worker publica evento a Mongo/cola.
  3. Marca outbox como procesado.

## Tipos de eventos

- **Dominio**: cambios relevantes de negocio (`LoanApproved`, `StockReserved`).
- **Integración**: formato estable para consumidores.
- **Auditoría**: quién hizo qué (`audit_logs`).

## Idempotencia

- Cada evento con `event_id` único.
- Consumidores toleran reintento.
- Upsert/unique key por `event_id` cuando aplique.

## Errores

- Falla SQL => no hay evento.
- SQL ok y falla publicación => outbox retry.

## Observabilidad mínima

- métricas: eventos pendientes, retries, dead-letter.
- trazas: `event_id`, `aggregate_id`, `occurred_at`.
