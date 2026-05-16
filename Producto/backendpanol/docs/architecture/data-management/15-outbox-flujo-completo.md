# 15 - Outbox: flujo completo, justificacion y limites

- Estado del documento: vigente
- Ultima verificacion: 2026-05-15
- Fuente de verdad: `OutboxService`, `OutboxWorker`, `OutboxJooqRepository`, `MongoOutboxPublisher`, `V20__outbox_events.sql`

## Objetivo

Explicar por que existe outbox, cuando se usa y por que no reemplaza `audit_log` ni logs tecnicos.

## Componentes

- PostgreSQL `outbox_events` como persistencia de eventos pendientes.
- Productor de eventos en casos de uso (`OutboxService.enqueue`).
- Repositorio SQL (`OutboxJooqRepository`).
- Worker asincrono (`OutboxWorker`).
- Publicador a Mongo (`MongoOutboxPublisher`).

## Flujo

1. Caso de uso realiza cambios de negocio en SQL.
2. En la misma transaccion inserta evento `PENDING` en outbox.
3. Commit exitoso deja estado + evento consistentes.
4. Worker toma pendientes y publica.
5. Si publica ok: `PROCESSED`.
6. Si falla: retry; si supera max intentos: `FAILED`.

## Cuando se usa

Outbox se ejecuta en condiciones normales para casos que requieren integracion/eventos, no solo ante errores.

## No es redundante con otras tablas

- `outbox_events`: entrega eventual de eventos.
- `audit_log`: auditoria funcional/compliance.
- logs tecnicos/system log: troubleshooting tecnico.
- colecciones operativas Mongo: lectura funcional/historica.

## Consultas operativas utiles

```sql
SELECT status, COUNT(*) FROM outbox_events GROUP BY status;
SELECT COUNT(*) FROM outbox_events WHERE retry_count > 0;
SELECT * FROM outbox_events WHERE status = 'FAILED' ORDER BY occurred_at ASC LIMIT 100;
```

