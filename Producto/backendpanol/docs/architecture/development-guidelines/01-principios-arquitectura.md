- Estado del documento: vigente
- Ultima verificacion: 2026-05-15
- Fuente de verdad: ver matriz canonica vigente y codigo fuente actual

# 01 - Principios de Arquitectura

## Objetivo

Asegurar que cada incremento preserve decisiones base:
- módulos por dominio,
- casos de uso aislados,
- infraestructura intercambiable,
- eventos confiables.

## Principios obligatorios

1. **Dominio primero**
- Reglas de negocio viven en `domain`/`application`, no en controladores.

2. **Dependencias hacia adentro**
- `api` e `infrastructure` dependen de `application/domain`.
- `domain` nunca depende de frameworks.

3. **Estado canónico en SQL**
- Entidades transaccionales críticas se resuelven en PostgreSQL.

4. **Eventos para trazabilidad, no para verdad transaccional**
- Mongo/eventos complementan, no reemplazan estado canónico.

5. **Evolución incremental sin big-bang**
- Refactors arquitectónicos por módulo, con feature parity.

## Regla de oro

Si una decisión mezcla reglas de negocio con detalles técnicos (DB, HTTP, framework), separar antes de mergear.

