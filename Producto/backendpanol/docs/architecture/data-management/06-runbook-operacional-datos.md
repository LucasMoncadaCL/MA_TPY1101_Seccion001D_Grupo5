- Estado del documento: vigente
- Ultima verificacion: 2026-05-15
- Fuente de verdad: ver matriz canonica vigente y codigo fuente actual

# 06 - Runbook Operacional de Datos

## 1. Antes de desplegar

- Validar migraciones Flyway en entorno de staging.
- Verificar compatibilidad jOOQ con schema actual.
- Revisar índices nuevos y costo de creación.
- Confirmar secretos/credenciales por entorno.

## 2. Despliegue

1. Deploy backend con migraciones.
2. Validar startup y `flyway_schema_history`.
3. Ejecutar smoke tests:
   - login,
   - CRUD clave,
   - lectura dashboard.
4. Verificar escritura en Mongo (`inventory_movements`, `audit_logs`, etc.).

## 3. Checklist post deploy

- SQL:
  - conteo de filas en tablas críticas,
  - locks/errores en logs,
  - latencia consultas principales.
- Mongo:
  - creación de documentos esperados,
  - crecimiento por colección,
  - índices activos.

## 4. Incidentes comunes

### SQL lento
- Revisar plan de ejecución (`EXPLAIN ANALYZE`).
- Ajustar/crear índice.
- Revisar N+1 desde backend.

### Mongo con crecimiento excesivo
- Auditar colecciones sin TTL.
- Aplicar archivado por fecha.
- Particionar por periodo lógico si aplica.

### Inconsistencia SQL vs Mongo
- Ejecutar script de conciliación por ventana temporal.
- Reprocesar eventos faltantes desde outbox o audit trail.

## 5. Backups y recuperación

- PostgreSQL:
  - backup lógico diario + snapshot infra.
  - pruebas de restore mensuales.
- MongoDB:
  - snapshot/backup por política del cluster.
  - pruebas de restore mensuales.

## 6. Gobierno y auditoría

- Cambios de esquema solo por migración versionada.
- Cambios de estructura Mongo documentados con `schema_version`.
- Mantener trazabilidad de decisiones técnicas (ADR).

