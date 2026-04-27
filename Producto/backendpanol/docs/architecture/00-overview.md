# Arquitectura Overview

## Contexto

Esta guia alinea la arquitectura deseada (segun lineamientos del proyecto Panol Salud) con el estado actual del repositorio `backendpanol`.

## Patrones arquitectonicos adoptados

1. Monolito modular
2. Hexagonal por modulo
3. Comunicacion por contratos/eventos entre modulos

## Forma actual del codigo

Paquete base actual:

- `com.panol_project.backendpanol`

Capas globales:

- `bootstrap/`
- `modules/`
- `shared/`

## Reglas de arquitectura

1. `modules/*` no depende de `bootstrap/*`.
2. `shared/*` no depende de `modules/*`.
3. Cada modulo encapsula su acceso a datos en `infrastructure/`.
4. Se priorizan contratos explicitos para comunicacion entre modulos.

## Estado de validacion automatica

Las reglas 1 y 2 se validan con ArchUnit:

- `src/test/java/com/panol_project/backendpanol/ArchitectureTest.java`

## Modulos

### Implementado hoy

- `catalog/category`

### Objetivo de expansion

- `identity_access`
- `catalog/implement`
- `locations`
- `inventory`
- `loans`
- `notifications`
- `reporting`
- `audit`
- `ai_assistant`

## Gap actual vs arquitectura objetivo

1. Solo hay un modulo de negocio implementado en produccion (`catalog/category`).
2. Aun no existe carpeta `docs/modules/` completa para todos los modulos objetivo.
3. No hay capa de eventos de dominio transversal documentada en `shared/events` (objetivo futuro).
4. El dominio actual mezcla DTOs de request/response en `domain`; a futuro se recomienda separar modelos de dominio de modelos de transporte.

## Principios para siguientes cambios

1. Cada nueva HU debe entrar en el modulo correcto y con capas `api/application/domain/infrastructure`.
2. Ninguna regla de dependencia debe romper los tests de arquitectura.
3. Si una integracion cruza modulos, primero definir contrato/evento y luego implementar adapter.
4. Documentar cada modulo nuevo en `docs/modules/{modulo}.md`.
