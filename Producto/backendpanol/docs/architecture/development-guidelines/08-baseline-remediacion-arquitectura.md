# 08 - Baseline Remediacion Arquitectonica

## Rama de trabajo

- `chore/architecture-hardening-full`

## Incumplimientos detectados al inicio

- Dependencias directas de `application` a `infrastructure`.
- Uso de `DSLContext` en `api`/`application`.
- Dependencias de framework en `domain`.
- Ausencia de tabla/worker Outbox.
- Reglas ArchUnit insuficientes para capas hexagonales y contratos cross-modulo.

## Estado de cierre aplicado

- `auth`: puertos de dominio (`UserAuthPort`, `TokenRevocationPort`, `AuditLogPort`) + adapters JOOQ.
- `users`: persistencia SQL extraida a `UserAdminRepository` + `UserAdminJooqRepository`.
- `catalog/stock`: movimientos por repositorio de dominio y adapter Mongo; etiquetas via contrato con `catalog/implement`.
- `catalog/implement`: consulta de movimientos via contrato explicito (`InventoryMovementQueryContract`).
- `domain` de movimientos desacoplado de framework.
- Outbox operativo base:
  - migracion `V20__outbox_events.sql`,
  - escritura de eventos desde casos criticos (`auth`, `users`, `inventory movement`),
  - worker con reintentos e idempotencia por `event_id`.
- ArchUnit endurecido para bloquear regresiones por capas y por frontera de modulo.

## Criterio de salida y pipeline

- [x] Cero `DSLContext` en `api`.
- [x] Cero imports `..infrastructure..` en `..application..`.
- [x] `..domain..` sin dependencias `spring/jooq/jakarta`.
- [x] `api`/`application` cross-modulo solo via `application.contract` (o excepcion documentada).
- [x] `domain` sin dependencia a `api/application/infrastructure`.
- [x] Outbox table + worker implementados.
- [x] Documentacion arquitectonica actualizada.
- [x] Pipeline debe fallar ante violacion ArchUnit.
