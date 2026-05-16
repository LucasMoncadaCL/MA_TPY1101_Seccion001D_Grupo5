# 05 - Backend: Integracion con Datos

- Estado del documento: vigente
- Ultima verificacion: 2026-05-15
- Fuente de verdad: servicios de aplicacion, `OutboxService`, `OutboxWorker`, `MongoOutboxPublisher`

## Patron vigente

1. Caso de uso valida input y permisos.
2. Escribe estado de negocio en SQL.
3. Encola evento outbox en la misma transaccion si corresponde.
4. Confirma transaccion.
5. Worker asincrono publica evento/proyeccion.

## Idempotencia

- `event_id` estable por evento.
- Publicacion con `upsert` por identificador logico en destino.
- Reintentos sin duplicar efecto funcional.

## Observabilidad minima

- Conteo por estado de outbox.
- Seguimiento de retries y `FAILED`.
- Correlacion por evento y agregado.

## Alcances

- Endpoints operativos principales consumen estado SQL.
- Vistas de trazabilidad/eventos consumen proyecciones asincronas.

## Compatibilidad

Este documento reemplaza la estrategia antigua "SQL y luego Mongo sincronico" como descripcion del estado actual.

