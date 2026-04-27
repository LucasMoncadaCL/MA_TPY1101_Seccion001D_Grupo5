# Arquitectura Backend

## Definicion

Este backend sigue un enfoque de:

- Monolito modular
- Hexagonal por modulo (ports & adapters)
- Comunicacion entre modulos por contratos/eventos

Objetivo: mantener simplicidad operativa hoy (un deploy) y dejar preparado el codigo para extraccion progresiva a microservicios.

## Capas globales del proyecto

- `bootstrap`: configuracion de arranque y wiring transversal.
- `modules`: dominio funcional por contexto de negocio.
- `shared`: cross-cutting estable (errores, seguridad, utilidades transversales).
- `target/generated-sources/jooq`: codigo generado desde PostgreSQL para persistencia type-safe.

## Estructura esperada por modulo

Cada modulo debe mantener esta separacion:

- `api`: adapters de entrada (REST).
- `application`: casos de uso/orquestacion.
- `domain`: modelo y contratos del dominio (sin acoplamiento a framework en el ideal objetivo).
- `infrastructure`: adapters de salida (repositorios, clientes externos, jOOQ).

## Reglas de dependencia (vigentes)

1. `modules/*` no depende de `bootstrap/*` (validado con ArchUnit).
2. `shared/*` no depende de `modules/*` (validado con ArchUnit).
3. La persistencia de cada modulo se implementa en `infrastructure`.
4. Se evita acoplamiento directo arbitrario entre modulos.

Referencia de tests: `src/test/java/com/panol_project/backendpanol/ArchitectureTest.java`.

## Estado actual del backend

- Modulo implementado: `modules/catalog/category`.
- Capas presentes en ese modulo: `api`, `application`, `domain`, `infrastructure`.
- Seguridad y errores transversales ya centralizados en `bootstrap` y `shared`.

## Modulos objetivo (roadmap)

- `identity_access`
- `catalog/implement`
- `locations`
- `inventory`
- `loans`
- `notifications`
- `reporting`
- `audit`
- `ai_assistant` (adapter/proxy hacia servicio externo)

## Documentacion complementaria

- `docs/architecture/00-overview.md`
- `docs/modules/catalog-category.md`
- `docs/BACKEND.md`
- `docs/ENVIRONMENT.md`
- `docs/DEPLOYMENT.md`
