# 03 - PostgreSQL: Guia Tecnica Vigente

- Estado del documento: vigente
- Ultima verificacion: 2026-05-15
- Fuente de verdad: migraciones Flyway y repositorios jOOQ de infraestructura

## Rol de PostgreSQL

PostgreSQL mantiene el estado canonico del sistema: usuarios, roles, catalogo, stock y auditoria funcional.

## Principios vigentes

1. Migraciones Flyway forward-only (`Vn__*.sql`).
2. jOOQ como acceso type-safe en infraestructura.
3. Integridad por constraints e indices.
4. Outbox transaccional en tabla `outbox_events`.

## Outbox en PostgreSQL

- Base en `V20__outbox_events.sql`.
- Estados: `PENDING`, `PROCESSED`, `FAILED`.
- Campos operativos: `retry_count`, `processed_at`, payload de evento.

## Buenas practicas

- Mantener consultas SQL encapsuladas en repositorios del modulo.
- No mezclar logica de negocio en infraestructura.
- Versionar cambios de esquema con migraciones idempotentes cuando aplique.

## Nota

Disenos antiguos basados en triggers genericos como unico mecanismo de emision de eventos se consideran contexto historico; el mecanismo vigente es caso de uso + outbox en misma transaccion.

