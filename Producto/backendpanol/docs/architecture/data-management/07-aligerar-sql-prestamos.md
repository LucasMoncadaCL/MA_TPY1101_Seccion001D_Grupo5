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

# 07 - Estrategia para Aligerar SQL en Préstamos

## Objetivo

Definir cómo mantener buen rendimiento en PostgreSQL cuando crezcan fuertemente:
- `loan`
- `loan_detail`
- `loan_detail_individual`

Sin perder consistencia transaccional ni trazabilidad de negocio.

---

## 1. Principio base

- **SQL sigue siendo la fuente de verdad** para préstamos y stock.
- “Aligerar” no significa sacar la lógica crítica de SQL.
- Significa reducir volumen operativo activo y optimizar acceso.

---

## 2. Señales de que hay que actuar

Aplicar plan cuando se cumplan uno o más:

- Consultas operativas > 300 ms sostenidas.
- Picos de CPU/IO en ventanas de alta demanda.
- Crecimiento mensual acelerado de `loan*`.
- `VACUUM/ANALYZE` cada vez más costoso.
- Índices muy grandes con baja selectividad.

---

## 3. Estrategia recomendada (por etapas)

## Etapa A: Optimización sin cambiar modelo

1. Revisar y ajustar índices:
- `loan(status)`
- `loan(due_date)` (parcial: `WHERE due_date IS NOT NULL`)
- `loan(requester_id)`
- `loan_detail(loan_id)`
- `loan_detail(implement_id)`
- `loan_detail_individual(loan_detail_id)`
- `loan_detail_individual(individual_id)`

2. Optimizar consultas frecuentes (`EXPLAIN ANALYZE`).
3. Asegurar `ANALYZE` actualizado y planificador saludable.

## Etapa B: Particionado de tablas grandes

Particionar por fecha (mensual/trimestral), preferentemente en `loan` por `created_at` o `scheduled_date`.

Beneficios:
- Consultas recientes leen menos datos.
- Mejor mantenimiento por partición.

Consideraciones:
- Mantener FK/joins claros entre tablas.
- Ajustar queries para aprovechar pruning de particiones.

## Etapa C: Archivado de histórico cerrado

Mover préstamos históricos cerrados (ejemplo: `completed/cancelled/rejected` con antigüedad > 12 meses) a tablas de archivo:

- `loan_archive`
- `loan_detail_archive`
- `loan_detail_individual_archive`

Patrón recomendado:
1. Copiar lote histórico.
2. Validar conteos y checksums.
3. Eliminar de tablas activas.
4. Registrar ejecución del job.

---

## 4. Política de retención sugerida

Operativo (tablas activas):
- 12 a 18 meses de préstamos.

Archivo SQL:
- Historial completo de negocio por cumplimiento.

MongoDB:
- Mantener `loan_events` y `audit_logs` como timeline y trazabilidad extendida.

---

## 5. Job de archivado (diseño)

Frecuencia:
- Mensual (ventana de bajo tráfico).

Lote:
- 10k–50k préstamos por ejecución (ajustable).

Seguridad:
- Transacciones por lote.
- Reintentos idempotentes.
- Tabla de control de corridas (`archive_job_runs`).

Campos de control mínimos:
- `run_id`
- `started_at`, `ended_at`
- `rows_loan`, `rows_detail`, `rows_detail_individual`
- `status`
- `error_message`

---

## 6. Riesgos y mitigaciones

1. Riesgo: pérdida de trazabilidad por corte incorrecto.
- Mitigación: validación previa/post por conteos y FK lógicas.

2. Riesgo: impacto en producción por lotes grandes.
- Mitigación: chunking, ventana nocturna, límites por tiempo.

3. Riesgo: reportes que mezclan activo + histórico.
- Mitigación: vista unificada (`UNION ALL`) o endpoint backend agregador.

4. Riesgo: consultas antiguas más complejas.
- Mitigación: capa de servicio dedicada para histórico.

---

## 7. Qué NO hacer

- No mover estado canónico vigente de préstamos a MongoDB.
- No borrar histórico sin política de retención aprobada.
- No hacer migraciones masivas sin rollback plan.

---

## 8. Plan mínimo de implementación

1. Baseline métricas actuales (latencia, tamaño, throughput).
2. Optimización de índices/queries.
3. Particionado `loan`.
4. Job de archivado mensual + tablas `_archive`.
5. Dashboard técnico de salud de datos.

---

## 9. Criterio de éxito

- Latencia p95 de endpoints de préstamos estable.
- Reducción de tamaño operativo en tablas activas.
- Sin inconsistencias entre `loan` y detalles.
- Historial disponible en archivo sin pérdida de integridad.


