## Advertencia historica

Este documento conserva contexto tecnico de una etapa anterior. No debe usarse como guia operativa primaria sin contrastar con la documentacion vigente.

## Estado actual (vigente)

- Contratos publicos: solo /api/v2/**.
- Seguridad: permitAll solo en POST /api/v2/auth/login (+ health/info).
- Eventos: outbox operativo con estados PENDING/PROCESSED/FAILED.
- Compose principal: Producto/docker-compose.yaml (frontend + backend, sin postgres local).
- Estado del documento: historico
- Ultima verificacion: 2026-05-15
- Fuente de verdad: ver matriz canonica vigente y codigo fuente actual

# Deuda TÃ©cnica UUID: Cierre por Prioridad

## P0 (bloquea corte DB definitivo)
1. `UserAdminService`: aÃºn resuelve internamente por `id` en queries y updates.
2. `UuidDomainResolver`: puente UUID->ID sigue siendo obligatorio en casi todo catÃ¡logo.
3. MÃ³dulos `ImplementService`, `StockService`, `InventoryMovementService`: dominio/repositorio centrado en `Integer`.
4. Repositorios jOOQ de catÃ¡logo (`ImplementJooqRepository`, `StockJooqRepository`, `CategoriaJooqRepository`, `LocationJooqRepository`) con PK/FK numÃ©ricas.

## P1 (contrato ya v2, pero falta pureza interna)
1. Auth/auditorÃ­a siguen persistiendo `actor_user_id` / `target_user_id` numÃ©ricos.
2. Barcode/stock/movements v2 dependen de controladores legacy internos.
3. DTOs legacy aÃºn existen y deben eliminarse al finalizar transiciÃ³n.

## P2 (front/docs/endurecimiento)
1. `utils/auth.ts` conserva helper de `getUserIdFromToken()` (debe retirarse si no hay uso).
2. DocumentaciÃ³n funcional antigua menciona endpoints `/api/*` no-v2.
3. Contratos docs deben consolidarse en UUID-only.

## Plan de cierre incremental
1. Migrar puertos/repositorios dominio a UUID en backend (sin cambiar funcionalidad).
2. Reemplazar servicios internos para no depender de `UuidDomainResolver` salvo compatibilidad temporal.
3. Eliminar controladores/DTO legacy del classpath pÃºblico y luego del cÃ³digo.
4. Ejecutar corte SQL (`uuid-final-cutover.sql`) cuando P0 estÃ© resuelto.



