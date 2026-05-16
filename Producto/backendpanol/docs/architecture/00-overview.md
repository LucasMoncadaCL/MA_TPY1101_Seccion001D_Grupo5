# Arquitectura Overview

- Estado del documento: vigente
- Ultima verificacion: 2026-05-15
- Fuente de verdad: `ArchitectureTest`, estructura de paquetes en `src/main/java`

## Patrones adoptados

1. Monolito modular.
2. Hexagonal por modulo.
3. Integracion entre modulos por contratos y eventos.

## Estructura global

- `bootstrap/`
- `modules/`
- `shared/`

## Reglas estructurales

1. `modules` no depende de `bootstrap`.
2. `shared` no depende de `modules`.
3. `domain` no depende de `api/application/infrastructure`.
4. Cross-modulo por `application.contract` o `domain.port`.

## Modulos implementados

- `auth`
- `users`
- `catalog/category`
- `catalog/location`
- `catalog/implement`
- `catalog/stock`

## Referencias

- `docs/architecture/00-matriz-canonica-vigente.md`
- `docs/architecture/development-guidelines/09-contratos-cross-modulo.md`
- `src/test/java/com/panol_project/backendpanol/ArchitectureTest.java`

