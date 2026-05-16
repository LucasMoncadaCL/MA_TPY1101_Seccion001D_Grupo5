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

# Modelo No Relacional (MongoDB) y Relación con SQL

## Objetivo del diseño
Este esquema propone un **modelo híbrido**:
- **SQL (relacional)** conserva las entidades maestras y transaccionales principales (`users`, `implements/products`, `loans`).
- **MongoDB (no relacional)** almacena eventos, trazabilidad, alertas y notificaciones con estructura flexible y lectura rápida por contexto.

La relación entre ambos mundos se hace mediante campos de referencia (`*_id`) en MongoDB que apuntan a IDs existentes en SQL.

## Resumen de colecciones MongoDB

### 1. `inventory_movements` (append-only)
Historial inmutable de movimientos de inventario.

Campos:
- `_id`: `ObjectId`
- `product_id`: `int` (referencia a `implements/products` en SQL)
- `movement_type`: `string`
- `quantity`: `int`
- `condition`: `string`
- `handled_by`: `int` (referencia a `users` en SQL)
- `notes`: `string`
- `created_at`: `Date`

Uso:
- Registro de entradas/salidas/ajustes sin sobrescribir historial.
- Fuente para auditoría operativa y reconstrucción de stock.

### 2. `stock_alerts` (efímera)
Estado actual e histórico breve de alertas de stock.

Campos:
- `_id`: `ObjectId`
- `implement_id`: `int` (referencia a `implements` en SQL)
- `current_stock`: `int`
- `min_stock`: `int`
- `created_at`: `Date`
- `resolved`: `boolean`
- `resolved_at`: `Date | null`

Uso:
- Disparar alertas por bajo stock.
- Flujo de resolución operativa (abierta/cerrada).

### 3. `notifications` (polimórfica)
Bandeja de notificaciones asociada a usuarios.

Campos:
- `_id`: `ObjectId`
- `user_id`: `int` (referencia a `users` en SQL)
- `type`: `string`
- `message`: `string`
- `read`: `boolean`
- `created_at`: `Date`
- `related_loan_id`: `int | null` (referencia a `loans` en SQL)
- `related_product_id`: `int | null` (referencia a `implements/products` en SQL)

Uso:
- Notificar eventos de distintos dominios usando una sola colección.
- El carácter polimórfico permite asociar la notificación a préstamo, producto u otra entidad.

### 4. `audit_logs` (append-only)
Bitácora de auditoría funcional y técnica.

Campos:
- `_id`: `ObjectId`
- `user_id`: `int` (referencia a `users` en SQL)
- `action`: `string`
- `entity_type`: `string`
- `entity_id`: `int`
- `logged_at`: `Date`
- `details`: `Object` (subdocumento embebido) con estructura sugerida:
  - `before`: `Object | null`
  - `after`: `Object | null`
  - `meta`: `Object | null`

Uso:
- Registrar quién hizo qué y sobre qué entidad.
- Guardar diferencias (`before/after`) sin alterar tablas SQL.

### 5. `loan_events` (historial embebido)
Historial de cambios de estado de préstamos.

Campos:
- `_id`: `ObjectId`
- `loan_id`: `int` (referencia a `loans` en SQL)
- `status_history`: `Array<subdocument>` embebido:
  - `from_status`: `string`
  - `to_status`: `string`
  - `changed_at`: `Date`
  - `changed_by`: `int` (referencia a `users` en SQL)
  - `comment`: `string | null`

Uso:
- Mantener el timeline de estados del préstamo en un solo documento.
- Favorecer lecturas directas del historial completo por `loan_id`.

## Cómo se relaciona MongoDB con el modelo relacional

### Entidades maestras en SQL (fuera de Mongo)
- `users`
- `implements/products`
- `loans`

MongoDB **no reemplaza** estas tablas; las complementa.

### Tipo de relación
- En SQL, las relaciones suelen reforzarse con claves foráneas.
- En MongoDB, la relación se implementa como **referencias por ID** (ej.: `user_id`, `loan_id`, `product_id`) sin `JOIN` nativo obligatorio.

### Patrón aplicado
Patrón híbrido **"referenciar + embeber"**:
- **Referenciar** cuando la entidad ya vive en SQL y tiene ciclo de vida propio.
- **Embeber** cuando los datos dependen del documento padre y conviene leerlos juntos.

## Embebidos vs referencias en este esquema

### Casos embebidos
- `loan_events.status_history[]` embebe eventos de estado dentro del préstamo lógico.
- `audit_logs.details` embebe snapshot/delta del cambio (`before`, `after`, `meta`).

Ventajas del embebido aquí:
- Menos consultas para reconstruir contexto.
- Coherencia de lectura por agregado (historial completo en el mismo documento).

### Casos referenciados
- `*_id` que apuntan a SQL (`user_id`, `loan_id`, `product_id`, `implement_id`, `changed_by`).

Ventajas de referenciar aquí:
- Evita duplicar entidades maestras.
- Mantiene una única fuente de verdad en SQL para catálogo, usuarios y préstamos.

## Decisiones clave del diseño

1. **Colecciones append-only** (`inventory_movements`, `audit_logs`)
   - No se reescribe historial.
   - Trazabilidad fuerte para auditoría.

2. **Colección efímera** (`stock_alerts`)
   - Orientada a operación diaria, puede limpiarse o archivarse por política.

3. **Colección polimórfica** (`notifications`)
   - Soporta múltiples tipos de evento en un modelo único.

4. **Historial embebido** (`loan_events`)
   - Priorización de lectura cronológica por préstamo.

## Reglas de consistencia recomendadas

- Validar en la capa de aplicación que cada `*_id` exista en SQL antes de insertar en MongoDB.
- Definir catálogos controlados para `action`, `entity_type`, `movement_type`, `type`.
- Versionar estructura de subdocumentos (`details`, `status_history`) para evolución segura.
- Indexar campos de consulta frecuente:
  - `inventory_movements.product_id`, `inventory_movements.created_at`
  - `stock_alerts.implement_id`, `stock_alerts.resolved`
  - `notifications.user_id`, `notifications.read`, `notifications.created_at`
  - `audit_logs.entity_type`, `audit_logs.entity_id`, `audit_logs.logged_at`
  - `loan_events.loan_id`

## Flujo típico entre SQL y MongoDB

1. Ocurre una operación principal en SQL (por ejemplo, cambio de estado de préstamo).
2. La aplicación persiste la transacción principal en SQL.
3. Como efecto de dominio, registra evento complementario en MongoDB:
   - historial (`loan_events`),
   - auditoría (`audit_logs`),
   - notificación (`notifications`),
   - movimiento de inventario (`inventory_movements`) si aplica.
4. Las vistas operativas consultan MongoDB para timelines/alertas/logs, y SQL para estado maestro.

## Conclusión
La arquitectura representada implementa una separación clara de responsabilidades:
- **SQL** para integridad relacional, entidades núcleo y reglas transaccionales.
- **MongoDB** para eventos, historial, auditoría y mensajería operacional.

El resultado es un sistema más flexible para trazabilidad y consultas de contexto, sin perder la fuente de verdad relacional.


