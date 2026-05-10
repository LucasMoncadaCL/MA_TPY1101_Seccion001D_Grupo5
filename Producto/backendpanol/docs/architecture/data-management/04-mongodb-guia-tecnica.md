# 04 - MongoDB: Guía Técnica

## 1. Objetivo

MongoDB soporta eventos, alertas, notificaciones y auditoría de alto volumen con estructura flexible.

## 2. Colecciones objetivo

- `inventory_movements`
- `loan_events`
- `audit_logs`
- `notifications`
- `stock_alerts`

## 3. Modelo por colección

### `inventory_movements` (append-only)
- Uso: trazabilidad de movimientos.
- Campos clave: `implement_id`, `movement_type`, `quantity`, `created_at`, `handled_by`.

### `loan_events`
- Uso: historial por préstamo.
- Documento por `loan_id` con arreglo `status_history`.

### `audit_logs` (append-only)
- Uso: bitácora funcional/técnica.
- Campos: `user_id`, `action`, `entity_type`, `entity_id`, `details`, `logged_at`.

### `notifications`
- Uso: bandeja por usuario.
- Campos: `user_id`, `type`, `message`, `read`, `created_at`.

### `stock_alerts`
- Uso: alertas activas/históricas de stock.
- Campos: `implement_id`, `current_stock`, `min_stock`, `resolved`, `created_at`.

## 4. Índices recomendados

- `inventory_movements`: `{ implement_id: 1, created_at: -1 }`
- `loan_events`: `{ loan_id: 1 }` UNIQUE
- `audit_logs`: `{ entity_type: 1, entity_id: 1, logged_at: -1 }`
- `notifications`: `{ user_id: 1, read: 1, created_at: -1 }`
- `stock_alerts`: `{ implement_id: 1, resolved: 1, created_at: -1 }`

## 5. TTL y retención

- `notifications`: TTL opcional (ej. 180 días) según política.
- `stock_alerts`: conservar abiertas + cerrar/archivar históricas.
- `audit_logs`: sin TTL si cumplimiento exige historial completo.

## 6. Validación de esquema

- Usar `JSON Schema` por colección para campos mínimos.
- Versionar documentos con `schema_version` cuando cambie estructura.

## 7. Consistencia con SQL

- IDs referenciados (`user_id`, `loan_id`, `implement_id`) deben existir en SQL.
- Validación en backend antes de insertar documento.
- Reconciliación nocturna recomendada para detectar referencias huérfanas.

## 8. Operación

- Monitorear crecimiento por colección.
- Revisar documentos grandes (`loan_events.status_history`), aplicar corte/archivo si excede límites operativos.
- Mantener estrategia de backup y restore probada.
