- Estado del documento: vigente
- Ultima verificacion: 2026-05-15
- Fuente de verdad: ver matriz canonica vigente y codigo fuente actual

# 10 - Runbook Operacional Outbox

## SeÃ±ales operacionales

- `pending_count`: eventos pendientes por publicar.
- `retry_count`: eventos con al menos un reintento.
- `failed_count`: eventos en estado `FAILED`.
- `publish_success_rate`: tasa de Ã©xito de publicaciÃ³n del worker.

Las mÃ©tricas se reportan en logs estructurados por `OutboxWorker`:
- `outbox_event_published`
- `outbox_event_publish_failed`
- `outbox_metrics`

## PolÃ­tica de reintentos

- `MAX_RETRIES = 5`.
- Mientras `retry_count < 5`, el estado se mantiene `PENDING`.
- Al llegar a 5 intentos fallidos, el evento pasa a `FAILED`.

## RecuperaciÃ³n manual (dead-letter lÃ³gico)

1. Identificar eventos `FAILED` en `outbox_events`.
2. Corregir causa raÃ­z de publicaciÃ³n (conectividad Mongo, payload invÃ¡lido, etc.).
3. Reencolar eventos fallidos:
   - set `status = 'PENDING'`
   - opcionalmente reset `retry_count = 0`
4. Monitorear logs `outbox_event_published` y `outbox_metrics`.

## Consultas Ãºtiles

```sql
SELECT status, COUNT(*) FROM outbox_events GROUP BY status;
SELECT COUNT(*) FROM outbox_events WHERE retry_count > 0;
SELECT * FROM outbox_events WHERE status = 'FAILED' ORDER BY occurred_at ASC LIMIT 100;
```

