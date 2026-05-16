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

# 11 - Corte UUID-Only: Plan de Ejecución y Estado

Fecha: 2026-05-10
Estado general: En ejecución por fases

## Objetivo
Completar el corte UUID-only end-to-end (backend, frontend, PostgreSQL y eventos), dejando `sub` como identidad canónica y retirando dependencias públicas de IDs numéricos.

## Resumen de estrategia
- P0: Endurecer contratos y auth UUID-only sin romper operación.
- P1: Migrar módulos restantes a consumo y persistencia UUID-first.
- P2: Ventana de corte destructivo en DB (retiro `*_id` legacy).

---

## P0 - Aplicado en esta iteración

### 1) JWT/Auth UUID-only
- `AuthService` deja de emitir claim `user_uuid`.
- JWT queda con `sub` como identidad canónica.
- `jwt-claims.md` actualizado a contrato UUID-only en autenticación.

### 2) Backend sin fallback `user_id` en claims
- `UserAdminService#getUserId(Jwt)` ya no lee `user_id` claim legacy.
- Resolución de actor queda basada en `sub` UUID.

### 3) Movimientos inventario sin hardcode de actor
- `InventoryMovementController` elimina `performedBy=1` hardcodeado.
- Ahora extrae `sub` (UUID) del JWT y resuelve `user.id` desde DB.

### 4) Frontend identity
- `utils/auth.ts` deja de usar fallback `user_uuid`.
- El frontend usa `sub` como fuente única de identidad de sesión.

---

## P1 - Próximos cambios requeridos

### Backend
1. Eliminar adaptadores `uuid -> id` donde ya exista repositorio UUID nativo.
2. Migrar préstamos/reportes/historial a repositorios UUID-first.
3. Marcar `/api/v1/**` como deprecated y bloquear nuevas integraciones.

### Frontend
1. Eliminar cualquier fallback `id ?? uuid` residual.
2. Confirmar rutas/hash y stores solo con `string` UUID.
3. Alinear todas las páginas no inventario a API `v2`.

### Eventos/Mongo
1. Estandarizar `aggregateId`, `entityId`, `actorId` como UUID string.
2. Retirar `legacyId` cuando no queden consumidores.

---

## P2 - Corte final (ventana controlada)

### Precondiciones
1. Backups PostgreSQL + export Mongo.
2. Validación de integridad UUID (`migration_check_*`, sin huérfanos).
3. Smoke tests verdes en entorno staging con API `v2`.

### Ejecución
1. Cambiar PK/FK efectivas a UUID en orden topológico.
2. Retirar columnas `*_id` de negocio y secuencias legacy.
3. Ajustar triggers, vistas, procedimientos y jobs a UUID-only.

### Post-corte
1. Retirar `/api/v1/**`.
2. Eliminar compatibilidad dual-key remanente.
3. Monitoreo intensivo de 4xx/5xx, locks y latencia.

---

## Checklist de aceptación UUID-only final
- [ ] Ningún endpoint público usa IDs numéricos de negocio.
- [ ] JWT usa `sub` como única identidad canónica.
- [ ] Frontend no convierte IDs de negocio a `number`.
- [ ] Eventos usan UUID-only.
- [ ] DB sin columnas `*_id` de negocio legacy.
- [ ] E2E completo verde (director/coordinador/docente).

---

## Riesgos y notas
- El retiro físico de `*_id` requiere ventana de mantenimiento y rollback probado.
- No ejecutar corte destructivo directo en producción sin staging UUID-only validado.


