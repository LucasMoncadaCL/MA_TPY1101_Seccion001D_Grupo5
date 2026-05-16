# 01 - Flujo End-to-End de Datos

- Estado del documento: vigente
- Ultima verificacion: 2026-05-15
- Fuente de verdad: servicios de aplicacion + `shared/outbox` + migracion `V20__outbox_events.sql`

## Flujo vigente

1. Cliente llama endpoint `/api/v2/**`.
2. Backend valida auth, autorizacion y reglas de negocio.
3. Caso de uso abre transaccion SQL.
4. Se persiste estado canonico en PostgreSQL.
5. Si el caso requiere integracion asincrona, se encola evento en `outbox_events` dentro de la misma transaccion.
6. Se confirma transaccion.
7. Worker de outbox procesa `PENDING` y publica a Mongo (u otro destino).
8. Estado outbox se actualiza a `PROCESSED` o reintenta/falla controladamente.

## Regla de oro

- Estado transaccional: PostgreSQL.
- Entrega de eventos: Outbox.
- Proyecciones y trazabilidad operativa: Mongo.

## Manejo de fallas

- Si falla SQL: no hay commit de negocio ni outbox.
- Si SQL confirma y falla publicacion: queda `PENDING`/retry sin perder evento.
- Si supera max reintentos: `FAILED` para tratamiento operativo.

## Nota de compatibilidad

No usar rutas legacy (`/api/categorias`, `/api/implements`, `/api/v1/**`) como contrato operativo actual.

