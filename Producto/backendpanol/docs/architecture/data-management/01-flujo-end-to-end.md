# 01 - Flujo End-to-End de Datos

## 1. Flujo general

1. Cliente (frontend) envía request al backend.
2. Backend valida autenticación/autorización y reglas de negocio.
3. Backend inicia transacción SQL (si aplica).
4. Backend persiste estado maestro en PostgreSQL.
5. Backend confirma transacción SQL.
6. Backend registra eventos complementarios en MongoDB.
7. Backend responde al cliente.
8. Lecturas ejecutivas/operativas combinan datos SQL + Mongo según caso.

## 2. Regla de oro

- **Primero SQL, después Mongo**.
- SQL consolida el estado canónico del sistema.
- Mongo guarda trazabilidad, timeline y mensajería operacional.

## 3. Casos clave

### 3.1 Crear/editar entidad maestra (ej. implemento)

- Escritura principal: PostgreSQL (`implement`, `stock`).
- Evento secundario: `audit_logs` en Mongo (`action=create|update`, `entity_type=implement`).

### 3.2 Movimiento de inventario

- Ajuste de stock: PostgreSQL (`stock`, posible relación con `loan_detail`).
- Registro de movimiento: Mongo `inventory_movements` (append-only).
- Alerta opcional: Mongo `stock_alerts` si cae bajo mínimo.

### 3.3 Flujo de préstamo

- Estado transaccional: PostgreSQL (`loan`, `loan_detail`, `loan_detail_individual`).
- Historial de estado: Mongo `loan_events.status_history[]`.
- Notificaciones: Mongo `notifications`.

## 4. Lecturas por tipo

- **Operativas exactas** (stock actual, disponibilidad): PostgreSQL.
- **Historial y trazabilidad** (timeline, logs, notificaciones): MongoDB.
- **Dashboard ejecutivo**: backend agrega y combina ambos según métrica.

## 5. Manejo de fallas

### SQL falla
- No escribir en Mongo.
- Responder error y registrar trazas técnicas.

### SQL OK, Mongo falla
- Responder éxito de operación maestra (si corresponde a negocio).
- Marcar evento pendiente de reproceso (outbox/retry recomendado).
- Reintentar asíncronamente con política exponencial.

## 6. Evolución recomendada

- Implementar patrón **Outbox** en PostgreSQL para garantizar entrega de eventos a Mongo.
- Introducir workers dedicados para reintento y conciliación.
