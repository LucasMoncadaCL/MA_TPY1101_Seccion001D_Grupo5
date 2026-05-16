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

# UUID Cutover Big-Bang: Backup y Rollback

Fecha: 2026-05-10  
Objetivo: ejecutar el corte UUID-only con recuperaciÃ³n garantizada.

## 1) Pre-condiciones

- Rama activa: `release/uuid-cutover`.
- Ventana de mantenimiento aprobada.
- Congelamiento de merges funcionales.
- Responsable tÃ©cnico de backend, frontend y datos asignado.

## 2) Backup obligatorio (antes del deploy)

1. Snapshot de PostgreSQL (proveedor/infra).
2. Dump lÃ³gico PostgreSQL:
```bash
pg_dump -h <host> -U <user> -d <db> -Fc -f pre_uuid_cutover.dump
```
3. Export de colecciones Mongo usadas por eventos/auditorÃ­a:
- `inventory_movements`
- `loan_events`
- `audit_logs`
- `notifications`
- `stock_alerts`

## 3) Checklist de release

1. Validar migraciones Flyway pendientes y orden.
2. Ejecutar deploy backend con esquema final UUID-only.
3. Ejecutar deploy frontend UUID-only.
4. Correr smoke test:
- login director/coordinador/docente
- CRUD usuarios director
- inventario (listado, detalle, ediciÃ³n)
- movimientos/stock

## 4) Criterios de rollback inmediato

Disparar rollback completo si ocurre cualquiera:
- falla de migraciÃ³n destructiva.
- >5% de 5xx en endpoints crÃ­ticos por mÃ¡s de 5 minutos.
- login/auth inoperable.
- corrupciÃ³n o inconsistencia de datos detectada en checks.

## 5) Procedimiento de rollback

1. Detener trÃ¡fico (frontend/backend).
2. Restaurar snapshot PostgreSQL (o `pg_restore` del dump).
3. Restaurar export Mongo si hubo cambios incompatibles.
4. Revertir backend/frontend a versiÃ³n previa estable.
5. Validar smoke test previo (v1).

## 6) Validaciones post-corte

Ejecutar:
- `migration_check_user_uuid`
- `migration_check_relationship_uuid`
- `fn_validate_uuid_migration()`

Todos los contadores deben ser `0`.


