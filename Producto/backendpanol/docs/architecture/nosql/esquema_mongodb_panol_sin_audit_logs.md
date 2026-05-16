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

# DiseÃ±o de Esquemas MongoDB para Sistema de PaÃ±ol

## VersiÃ³n ajustada sin `audit_logs` en MongoDB

---

## 1. PropÃ³sito del documento

Este documento define el diseÃ±o recomendado para las colecciones de **MongoDB** dentro del sistema de paÃ±ol, considerando que la base de datos principal y transaccional del proyecto es **PostgreSQL**.

La decisiÃ³n principal de esta versiÃ³n es que **MongoDB no manejarÃ¡ auditorÃ­a formal de acciones de usuario**, porque esa responsabilidad ya estÃ¡ cubierta por la tabla relacional `audit_log` en PostgreSQL.

Por lo tanto, MongoDB se utilizarÃ¡ Ãºnicamente para:

```txt
MongoDB
â”œâ”€â”€ inventory_movements
â”‚   â””â”€â”€ Historial de movimientos de inventario
â”‚
â”œâ”€â”€ loan_events
â”‚   â””â”€â”€ Timeline de cambios de estado de prÃ©stamos
â”‚
â”œâ”€â”€ notifications
â”‚   â””â”€â”€ Notificaciones internas para usuarios
â”‚
â”œâ”€â”€ stock_alerts
â”‚   â””â”€â”€ Alertas de stock bajo, crÃ­tico o inconsistente
â”‚
â””â”€â”€ system_events
    â””â”€â”€ Eventos tÃ©cnicos internos del sistema
```

La idea es mantener una separaciÃ³n clara de responsabilidades:

```txt
PostgreSQL = datos principales, relaciones, reglas, transacciones y auditorÃ­a formal.
MongoDB    = historial operativo, eventos, notificaciones, alertas y observabilidad tÃ©cnica.
```

---

## 2. Contexto general de la arquitectura

El sistema de paÃ±ol trabaja con entidades principales como usuarios, roles, implementos, unidades individuales, stock, prÃ©stamos, detalles de prÃ©stamo, salas, categorÃ­as, ubicaciones y auditorÃ­a.

Estas entidades viven en PostgreSQL porque requieren:

- Integridad referencial.
- Relaciones entre tablas.
- Restricciones.
- Validaciones fuertes.
- Transacciones.
- Consistencia del estado actual.

MongoDB se utilizarÃ¡ como una base complementaria para registrar informaciÃ³n que crece histÃ³ricamente, que puede variar en estructura o que se consulta comÃºnmente por fechas, entidades relacionadas o eventos.

---

## 3. SeparaciÃ³n de responsabilidades

### 3.1. PostgreSQL

PostgreSQL debe mantener el estado oficial del sistema.

Ejemplos:

```txt
PostgreSQL
â”œâ”€â”€ user
â”œâ”€â”€ role
â”œâ”€â”€ implement
â”œâ”€â”€ individual
â”œâ”€â”€ stock
â”œâ”€â”€ loan
â”œâ”€â”€ loan_detail
â”œâ”€â”€ loan_detail_individual
â”œâ”€â”€ category
â”œâ”€â”€ location
â”œâ”€â”€ room
â”œâ”€â”€ token_revocation
â””â”€â”€ audit_log
```

En PostgreSQL deben quedar:

- Usuarios y roles.
- Implementos e inventario actual.
- Unidades individuales.
- Stock actual.
- PrÃ©stamos y sus estados actuales.
- Detalles de prÃ©stamo.
- Salas, categorÃ­as y ubicaciones.
- RevocaciÃ³n de tokens.
- AuditorÃ­a formal mediante `audit_log`.

---

### 3.2. MongoDB

MongoDB debe guardar informaciÃ³n complementaria, histÃ³rica u operativa.

```txt
MongoDB
â”œâ”€â”€ inventory_movements
â”œâ”€â”€ loan_events
â”œâ”€â”€ notifications
â”œâ”€â”€ stock_alerts
â””â”€â”€ system_events
```

En MongoDB deben quedar:

- Movimientos histÃ³ricos de inventario.
- Timeline de cambios de estado de prÃ©stamos.
- Notificaciones internas por usuario.
- Alertas operativas de stock.
- Eventos tÃ©cnicos internos del sistema.

MongoDB no debe reemplazar el estado principal de PostgreSQL.

---

## 4. DecisiÃ³n importante: no usar `audit_logs` en MongoDB

En versiones anteriores del diseÃ±o podÃ­a considerarse una colecciÃ³n `audit_logs` en MongoDB. Sin embargo, para este proyecto no se recomienda incluirla, porque ya existe una tabla `audit_log` en PostgreSQL.

Esto evita duplicar responsabilidades.

La auditorÃ­a formal debe quedarse en PostgreSQL.

```txt
PostgreSQL
â””â”€â”€ audit_log
    â””â”€â”€ AuditorÃ­a formal de acciones funcionales del sistema
```

MongoDB, en cambio, debe enfocarse en eventos operativos y tÃ©cnicos que no necesariamente representan auditorÃ­a formal.

---

## 5. Diferencia entre `audit_log` y `system_events`

Es importante no confundir la tabla `audit_log` de PostgreSQL con la colecciÃ³n `system_events` de MongoDB.

### 5.1. `audit_log` en PostgreSQL

Representa acciones funcionales o de negocio.

Responde preguntas como:

```txt
Â¿QuiÃ©n hizo quÃ©?
Â¿Sobre quÃ© entidad lo hizo?
Â¿CuÃ¡ndo ocurriÃ³?
Â¿QuÃ© usuario fue afectado?
```

Ejemplos:

```txt
- Un administrador creÃ³ un usuario.
- Un coordinador aprobÃ³ un prÃ©stamo.
- Un paÃ±olero actualizÃ³ un implemento.
- Un usuario fue desactivado.
- Un prÃ©stamo fue rechazado.
```

### 5.2. `system_events` en MongoDB

Representa eventos tÃ©cnicos internos del sistema.

Responde preguntas como:

```txt
Â¿QuÃ© ocurriÃ³ tÃ©cnicamente dentro del sistema?
Â¿QuÃ© proceso fallÃ³?
Â¿QuÃ© mÃ³dulo generÃ³ un error?
QuÃ© evento automÃ¡tico se ejecutÃ³?
```

Ejemplos:

```txt
- FallÃ³ el envÃ­o de una notificaciÃ³n.
- No se pudo registrar un movimiento en MongoDB.
- Se ejecutÃ³ un proceso automÃ¡tico de revisiÃ³n de stock.
- Se generÃ³ una alerta automÃ¡ticamente.
- OcurriÃ³ un error de sincronizaciÃ³n entre PostgreSQL y MongoDB.
- FallÃ³ un proceso batch.
```

### 5.3. ComparaciÃ³n rÃ¡pida

| Elemento | UbicaciÃ³n | PropÃ³sito |
|---|---|---|
| `audit_log` | PostgreSQL | AuditorÃ­a formal de acciones funcionales |
| `system_events` | MongoDB | Observabilidad tÃ©cnica interna |
| `inventory_movements` | MongoDB | Historial operativo de inventario |
| `loan_events` | MongoDB | Timeline de prÃ©stamos |
| `notifications` | MongoDB | Notificaciones internas |
| `stock_alerts` | MongoDB | Alertas operativas de stock |

---

## 6. Estrategia UUID-only

Como PostgreSQL usa UUID como identificador principal en sus tablas, MongoDB tambiÃ©n debe referenciar esas entidades mediante UUID.

No se recomienda usar identificadores enteros como:

```js
user_id: 1
implement_id: 5
loan_id: 10
```

La forma correcta es usar:

```js
user_uuid: "550e8400-e29b-41d4-a716-446655440000"
implement_uuid: "550e8400-e29b-41d4-a716-446655440001"
loan_uuid: "550e8400-e29b-41d4-a716-446655440002"
```

### 6.1. ConvenciÃ³n de nombres

Se recomienda usar la convenciÃ³n:

```txt
<entidad>_uuid
```

Ejemplos:

```txt
user_uuid
performed_by_uuid
changed_by_uuid
resolved_by_uuid
implement_uuid
individual_uuid
loan_uuid
loan_detail_uuid
stock_uuid
room_uuid
```

Esto permite que el modelo MongoDB sea coherente con el modelo relacional.

---

# 7. ColecciÃ³n `inventory_movements`

## 7.1. PropÃ³sito

La colecciÃ³n `inventory_movements` almacena el historial de movimientos de inventario.

Esta colecciÃ³n permite saber quÃ© ocurriÃ³ con los implementos, cuÃ¡ndo ocurriÃ³, quÃ© cantidad fue afectada, quiÃ©n realizÃ³ la acciÃ³n y si el movimiento estuvo asociado a un prÃ©stamo.

Debe funcionar como una colecciÃ³n **append-only**, es decir, los documentos normalmente no se editan ni se eliminan. Cada movimiento queda registrado como evidencia histÃ³rica.

---

## 7.2. Casos de uso

Esta colecciÃ³n puede registrar:

```txt
- Entrada manual de stock.
- Salida manual de stock.
- Ajuste de inventario.
- Reserva de implementos.
- Entrega de prÃ©stamo.
- DevoluciÃ³n de prÃ©stamo.
- Cambio de estado de una unidad individual.
- Cambio de condiciÃ³n de una unidad individual.
- Registro de daÃ±o.
- Registro de pÃ©rdida.
- CorrecciÃ³n administrativa.
- Movimiento asociado a cambio de ubicaciÃ³n.
```

---

## 7.3. Esquema recomendado

```js
{
  _id: ObjectId,

  implement_uuid: UUID,
  individual_uuid: UUID | null,
  loan_uuid: UUID | null,
  loan_detail_uuid: UUID | null,

  action: String,
  movement_type: String,

  quantity: Number,

  previous_stock: Number | null,
  new_stock: Number | null,

  previous_status: String | null,
  new_status: String | null,

  previous_condition: String | null,
  new_condition: String | null,

  performed_by_uuid: UUID | null,
  source: String,

  notes: String | null,

  metadata: {
    reason: String | null,
    client_ip: String | null,
    user_agent: String | null,
    request_id: String | null
  },

  created_at: Date,
  schema_version: Number
}
```

---

## 7.4. Campos principales

### `_id`

Identificador propio de MongoDB.

### `implement_uuid`

Referencia lÃ³gica a `public.implement.uuid` en PostgreSQL.

Este campo debe ser obligatorio, porque todo movimiento de inventario debe estar asociado a un implemento.

### `individual_uuid`

Referencia lÃ³gica a `public.individual.uuid`.

Se usa cuando el movimiento afecta a una unidad fÃ­sica especÃ­fica. Por ejemplo, un implemento reutilizable con cÃ³digo de activo propio.

Debe ser `null` cuando el movimiento afecta stock general o fungible.

### `loan_uuid`

Referencia lÃ³gica a `public.loan.uuid`.

Se usa cuando el movimiento estÃ¡ asociado a un prÃ©stamo.

### `loan_detail_uuid`

Referencia lÃ³gica a `public.loan_detail.uuid`.

Permite identificar quÃ© detalle especÃ­fico del prÃ©stamo generÃ³ el movimiento.

### `action`

AcciÃ³n realizada.

Valores sugeridos:

```txt
manual_adjustment
stock_entry
stock_exit
reserved
delivered
returned
damaged
lost
stock_correction
location_changed
condition_changed
status_changed
```

### `movement_type`

Indica el efecto general del movimiento.

Valores sugeridos:

```txt
in       = entra stock o vuelve al inventario
out      = sale stock o queda prestado
neutral  = no cambia cantidad, pero cambia estado, condiciÃ³n o ubicaciÃ³n
```

### `quantity`

Cantidad afectada.

Para unidades individuales suele ser `1`. Para stock fungible puede ser cualquier nÃºmero mayor a cero.

### `previous_stock` y `new_stock`

Permiten guardar el stock antes y despuÃ©s del movimiento.

Son Ãºtiles para reconstruir el historial de cambios.

### `previous_status` y `new_status`

Se usan cuando se modifica el estado de una unidad individual.

Ejemplo:

```txt
previous_status: "available"
new_status: "loaned"
```

### `previous_condition` y `new_condition`

Se usan cuando se modifica la condiciÃ³n fÃ­sica de una unidad.

Ejemplo:

```txt
previous_condition: "good"
new_condition: "damaged"
```

### `performed_by_uuid`

Referencia lÃ³gica al usuario que ejecutÃ³ la acciÃ³n.

Puede ser `null` si fue generada automÃ¡ticamente por el sistema.

### `source`

Origen del movimiento.

Valores sugeridos:

```txt
system
admin
loan_flow
stock_module
inventory_module
migration
```

### `notes`

Comentario opcional.

### `metadata`

InformaciÃ³n tÃ©cnica o contextual adicional.

### `created_at`

Fecha y hora del movimiento.

### `schema_version`

VersiÃ³n del esquema.

Permite evolucionar la colecciÃ³n a futuro.

---

## 7.5. Ejemplo de documento

```js
{
  _id: ObjectId("665f1c4b9d3f2a0012a44e10"),

  implement_uuid: "b7b642ef-ec13-4d2d-bdf5-3e7d22d7a111",
  individual_uuid: null,
  loan_uuid: "3ab1a4e2-54b7-47c8-8f71-8c88ab91a888",
  loan_detail_uuid: "6fa8d3f0-cbee-4f38-84f7-7e72e2d6e111",

  action: "delivered",
  movement_type: "out",

  quantity: 2,

  previous_stock: 10,
  new_stock: 8,

  previous_status: null,
  new_status: null,

  previous_condition: null,
  new_condition: null,

  performed_by_uuid: "1ae82916-1280-4c9a-b52a-6f43fdc6b111",
  source: "loan_flow",

  notes: "Entrega asociada a prÃ©stamo aprobado.",

  metadata: {
    reason: "loan_delivery",
    client_ip: "192.168.1.20",
    user_agent: "Mozilla/5.0",
    request_id: "req-2026-001"
  },

  created_at: ISODate("2026-05-11T14:30:00Z"),
  schema_version: 1
}
```

---

## 7.6. Ãndices recomendados

```js
db.inventory_movements.createIndex({ implement_uuid: 1, created_at: -1 })
db.inventory_movements.createIndex({ individual_uuid: 1, created_at: -1 })
db.inventory_movements.createIndex({ loan_uuid: 1, created_at: -1 })
db.inventory_movements.createIndex({ performed_by_uuid: 1, created_at: -1 })
db.inventory_movements.createIndex({ created_at: -1 })
```

---

# 8. ColecciÃ³n `loan_events`

## 8.1. PropÃ³sito

La colecciÃ³n `loan_events` almacena el timeline de cambios de estado de los prÃ©stamos.

PostgreSQL mantiene el estado actual del prÃ©stamo en la tabla `loan`. MongoDB guarda la historia completa de cÃ³mo ese prÃ©stamo llegÃ³ a su estado actual.

Por ejemplo:

```txt
PostgreSQL dice:
El prÃ©stamo estÃ¡ entregado.

MongoDB permite ver:
El prÃ©stamo fue creado, luego aprobado, luego entregado.
```

---

## 8.2. Estructura recomendada

Se recomienda usar un documento por prÃ©stamo, con un arreglo embebido `status_history`.

```txt
loan_events
â””â”€â”€ documento por prÃ©stamo
    â””â”€â”€ status_history[]
```

Esto es coherente porque el historial de estado pertenece directamente al prÃ©stamo.

---

## 8.3. Esquema recomendado

```js
{
  _id: ObjectId,

  loan_uuid: UUID,

  requester_uuid: UUID | null,
  room_uuid: UUID | null,

  current_status: String,

  status_history: [
    {
      from_status: String | null,
      to_status: String,

      changed_at: Date,
      changed_by_uuid: UUID | null,

      comment: String | null,
      reason: String | null,

      metadata: {
        source: String | null,
        client_ip: String | null,
        user_agent: String | null,
        request_id: String | null
      }
    }
  ],

  created_at: Date,
  updated_at: Date,
  schema_version: Number
}
```

---

## 8.4. Campos principales

### `loan_uuid`

Referencia lÃ³gica a `public.loan.uuid`.

Debe ser Ãºnico en esta colecciÃ³n.

### `requester_uuid`

Usuario que solicitÃ³ el prÃ©stamo.

Referencia lÃ³gica a `public.user.uuid`.

### `room_uuid`

Sala asociada al prÃ©stamo.

Referencia lÃ³gica a `public.room.uuid`.

### `current_status`

Estado actual del prÃ©stamo.

Debe reflejar el estado actual registrado en PostgreSQL, pero la fuente oficial sigue siendo la tabla `loan`.

Valores sugeridos:

```txt
pending
approved
rejected
cancelled
delivered
completed
```

### `status_history`

Arreglo de eventos de cambio de estado.

### `from_status`

Estado anterior.

Puede ser `null` cuando se registra la creaciÃ³n del prÃ©stamo.

### `to_status`

Nuevo estado del prÃ©stamo.

### `changed_at`

Fecha y hora del cambio.

### `changed_by_uuid`

Usuario que ejecutÃ³ el cambio.

Puede ser `null` si fue una acciÃ³n automÃ¡tica del sistema.

### `comment`

Comentario funcional del evento.

### `reason`

RazÃ³n controlada del cambio.

Ejemplos:

```txt
loan_created
approved_by_coordinator
rejected_due_to_no_stock
cancelled_by_requester
delivered_by_staff
completed_after_return
```

---

## 8.5. Ejemplo de documento

```js
{
  _id: ObjectId("665f1d889d3f2a0012a44e20"),

  loan_uuid: "3ab1a4e2-54b7-47c8-8f71-8c88ab91a888",
  requester_uuid: "9128d2de-b9d6-4d54-8f2c-42f5f71eaaaa",
  room_uuid: "ea83ce2a-2468-405a-a299-9de14c89bbbb",

  current_status: "delivered",

  status_history: [
    {
      from_status: null,
      to_status: "pending",
      changed_at: ISODate("2026-05-11T12:00:00Z"),
      changed_by_uuid: "9128d2de-b9d6-4d54-8f2c-42f5f71eaaaa",
      comment: "Solicitud creada por docente.",
      reason: "loan_created",
      metadata: {
        source: "frontend",
        client_ip: "192.168.1.15",
        user_agent: "Mozilla/5.0",
        request_id: "req-2026-100"
      }
    },
    {
      from_status: "pending",
      to_status: "approved",
      changed_at: ISODate("2026-05-11T13:00:00Z"),
      changed_by_uuid: "1ae82916-1280-4c9a-b52a-6f43fdc6b111",
      comment: "PrÃ©stamo aprobado por coordinaciÃ³n.",
      reason: "approved_by_coordinator",
      metadata: {
        source: "admin_panel",
        client_ip: "192.168.1.20",
        user_agent: "Mozilla/5.0",
        request_id: "req-2026-101"
      }
    }
  ],

  created_at: ISODate("2026-05-11T12:00:00Z"),
  updated_at: ISODate("2026-05-11T13:00:00Z"),
  schema_version: 1
}
```

---

## 8.6. Ãndices recomendados

```js
db.loan_events.createIndex({ loan_uuid: 1 }, { unique: true })
db.loan_events.createIndex({ requester_uuid: 1, updated_at: -1 })
db.loan_events.createIndex({ current_status: 1, updated_at: -1 })
db.loan_events.createIndex({ "status_history.changed_at": -1 })
```

---

# 9. ColecciÃ³n `notifications`

## 9.1. PropÃ³sito

La colecciÃ³n `notifications` almacena notificaciones internas para usuarios del sistema.

Permite implementar una bandeja de avisos, alertas o mensajes funcionales dentro del sistema.

---

## 9.2. Casos de uso

Ejemplos:

```txt
- Tu prÃ©stamo fue aprobado.
- Tu prÃ©stamo fue rechazado.
- Tu prÃ©stamo estÃ¡ listo para retiro.
- Hay una solicitud pendiente de aprobaciÃ³n.
- Hay stock bajo de un implemento.
- Se completÃ³ un prÃ©stamo.
- Se registrÃ³ una devoluciÃ³n pendiente.
```

---

## 9.3. Esquema recomendado

```js
{
  _id: ObjectId,

  user_uuid: UUID,

  type: String,
  title: String,
  message: String,

  read: Boolean,
  read_at: Date | null,

  priority: String,

  related_entity: {
    entity_type: String | null,
    entity_uuid: UUID | null
  },

  related_loan_uuid: UUID | null,
  related_implement_uuid: UUID | null,
  related_individual_uuid: UUID | null,

  action_url: String | null,

  metadata: {
    source: String | null,
    extra: Object | null
  },

  created_at: Date,
  expires_at: Date | null,
  schema_version: Number
}
```

---

## 9.4. Campos principales

### `user_uuid`

Usuario destinatario de la notificaciÃ³n.

Referencia lÃ³gica a `public.user.uuid`.

### `type`

Tipo de notificaciÃ³n.

Valores sugeridos:

```txt
loan_created
loan_approved
loan_rejected
loan_cancelled
loan_delivered
loan_completed
stock_low
stock_critical
system_message
pending_review
```

### `title`

TÃ­tulo corto de la notificaciÃ³n.

### `message`

Mensaje visible para el usuario.

### `read`

Indica si la notificaciÃ³n fue leÃ­da.

### `read_at`

Fecha de lectura.

Debe ser `null` si todavÃ­a no fue leÃ­da.

### `priority`

Nivel de prioridad.

Valores sugeridos:

```txt
low
normal
high
critical
```

### `related_entity`

Referencia genÃ©rica a una entidad relacionada.

Ejemplo:

```js
related_entity: {
  entity_type: "loan",
  entity_uuid: "3ab1a4e2-54b7-47c8-8f71-8c88ab91a888"
}
```

### `related_loan_uuid`

Referencia directa opcional a un prÃ©stamo.

### `related_implement_uuid`

Referencia directa opcional a un implemento.

### `related_individual_uuid`

Referencia directa opcional a una unidad individual.

### `action_url`

Ruta interna del frontend a la que puede llevar la notificaciÃ³n.

### `metadata`

InformaciÃ³n adicional.

### `expires_at`

Fecha opcional de expiraciÃ³n.

Puede usarse con un Ã­ndice TTL.

---

## 9.5. Ejemplo de documento

```js
{
  _id: ObjectId("665f1f3e9d3f2a0012a44e30"),

  user_uuid: "9128d2de-b9d6-4d54-8f2c-42f5f71eaaaa",

  type: "loan_approved",
  title: "PrÃ©stamo aprobado",
  message: "Tu prÃ©stamo fue aprobado y estÃ¡ listo para ser retirado en el paÃ±ol.",

  read: false,
  read_at: null,

  priority: "normal",

  related_entity: {
    entity_type: "loan",
    entity_uuid: "3ab1a4e2-54b7-47c8-8f71-8c88ab91a888"
  },

  related_loan_uuid: "3ab1a4e2-54b7-47c8-8f71-8c88ab91a888",
  related_implement_uuid: null,
  related_individual_uuid: null,

  action_url: "/loans/3ab1a4e2-54b7-47c8-8f71-8c88ab91a888",

  metadata: {
    source: "loan_flow",
    extra: {
      previous_status: "pending",
      new_status: "approved"
    }
  },

  created_at: ISODate("2026-05-11T13:05:00Z"),
  expires_at: null,
  schema_version: 1
}
```

---

## 9.6. Ãndices recomendados

```js
db.notifications.createIndex({ user_uuid: 1, read: 1, created_at: -1 })
db.notifications.createIndex({ user_uuid: 1, created_at: -1 })
db.notifications.createIndex({ related_loan_uuid: 1 }, { sparse: true })
db.notifications.createIndex({ related_implement_uuid: 1 }, { sparse: true })
db.notifications.createIndex({ expires_at: 1 }, { expireAfterSeconds: 0 })
```

---

# 10. ColecciÃ³n `stock_alerts`

## 10.1. PropÃ³sito

La colecciÃ³n `stock_alerts` almacena alertas de stock bajo, crÃ­tico o inconsistente.

PostgreSQL mantiene el estado actual del stock. MongoDB puede guardar el historial y estado operativo de las alertas generadas a partir de ese stock.

---

## 10.2. Casos de uso

```txt
- Alertas de bajo stock.
- Alertas de stock crÃ­tico.
- Alertas por exceso de unidades daÃ±adas.
- Alertas por inconsistencias entre stock total, disponible, reservado y prestado.
- Historial de alertas resueltas.
- Dashboard para paÃ±olero, coordinador o director.
```

---

## 10.3. Esquema recomendado

```js
{
  _id: ObjectId,

  implement_uuid: UUID,
  stock_uuid: UUID | null,

  alert_type: String,
  severity: String,

  current_stock: Number,
  min_stock: Number,

  available: Number,
  reserved: Number,
  loaned: Number,
  damaged: Number,

  message: String,

  resolved: Boolean,
  resolved_at: Date | null,
  resolved_by_uuid: UUID | null,
  resolution_notes: String | null,

  created_at: Date,
  updated_at: Date,
  schema_version: Number
}
```

---

## 10.4. Campos principales

### `implement_uuid`

Referencia lÃ³gica a `public.implement.uuid`.

### `stock_uuid`

Referencia lÃ³gica a `public.stock.uuid`.

Puede ser opcional si la alerta se quiere asociar principalmente al implemento.

### `alert_type`

Tipo de alerta.

Valores sugeridos:

```txt
low_stock
critical_stock
stock_inconsistency
damaged_excess
reserved_excess
loaned_excess
```

### `severity`

Nivel de gravedad.

Valores sugeridos:

```txt
info
warning
critical
```

### `current_stock`

Stock disponible o relevante al momento de generar la alerta.

### `min_stock`

Stock mÃ­nimo configurado.

### `available`, `reserved`, `loaned`, `damaged`

Snapshot del estado del stock al momento de generar la alerta.

Esto permite conservar contexto histÃ³rico aunque el stock cambie despuÃ©s.

### `message`

Mensaje descriptivo.

### `resolved`

Indica si la alerta fue resuelta.

### `resolved_at`

Fecha de resoluciÃ³n.

### `resolved_by_uuid`

Usuario que resolviÃ³ la alerta.

### `resolution_notes`

Comentario de resoluciÃ³n.

---

## 10.5. Ejemplo de documento

```js
{
  _id: ObjectId("665f20af9d3f2a0012a44e40"),

  implement_uuid: "b7b642ef-ec13-4d2d-bdf5-3e7d22d7a111",
  stock_uuid: "7888f158-e721-48d1-b73a-0a7be993cccc",

  alert_type: "low_stock",
  severity: "warning",

  current_stock: 3,
  min_stock: 5,

  available: 3,
  reserved: 2,
  loaned: 8,
  damaged: 1,

  message: "El implemento estÃ¡ por debajo del stock mÃ­nimo configurado.",

  resolved: false,
  resolved_at: null,
  resolved_by_uuid: null,
  resolution_notes: null,

  created_at: ISODate("2026-05-11T14:00:00Z"),
  updated_at: ISODate("2026-05-11T14:00:00Z"),
  schema_version: 1
}
```

---

## 10.6. Ãndices recomendados

```js
db.stock_alerts.createIndex({ implement_uuid: 1, resolved: 1, created_at: -1 })
db.stock_alerts.createIndex({ resolved: 1, severity: 1, created_at: -1 })
db.stock_alerts.createIndex({ alert_type: 1, created_at: -1 })
```

---

# 11. ColecciÃ³n `system_events`

## 11.1. PropÃ³sito

La colecciÃ³n `system_events` almacena eventos tÃ©cnicos internos del sistema.

No es auditorÃ­a funcional. No reemplaza a `audit_log`.

Sirve para observabilidad, diagnÃ³stico, seguimiento de errores tÃ©cnicos y registro de procesos automÃ¡ticos.

---

## 11.2. Casos de uso

```txt
- FallÃ³ el envÃ­o de una notificaciÃ³n.
- FallÃ³ el registro de un movimiento en MongoDB.
- OcurriÃ³ timeout al escribir en MongoDB.
- Se ejecutÃ³ un proceso automÃ¡tico de revisiÃ³n de stock.
- Se generÃ³ una alerta automÃ¡ticamente.
- FallÃ³ una sincronizaciÃ³n entre PostgreSQL y MongoDB.
- Un proceso batch terminÃ³ correctamente.
- Un proceso batch terminÃ³ con error.
- Se detectÃ³ una inconsistencia tÃ©cnica.
```

---

## 11.3. Esquema recomendado

```js
{
  _id: ObjectId,

  event_type: String,
  level: String,

  module: String,

  message: String,

  related_entity: {
    entity_type: String | null,
    entity_uuid: UUID | null
  },

  error: {
    code: String | null,
    message: String | null,
    stack: String | null
  },

  metadata: Object | null,

  created_at: Date,
  schema_version: Number
}
```

---

## 11.4. Campos principales

### `event_type`

Tipo de evento tÃ©cnico.

Valores sugeridos:

```txt
sync_error
notification_failed
job_executed
stock_alert_generated
mongo_write_failed
sql_transaction_completed
background_task_started
background_task_finished
background_task_failed
```

### `level`

Nivel del evento.

Valores sugeridos:

```txt
info
warning
error
critical
```

### `module`

MÃ³dulo que generÃ³ el evento.

Valores sugeridos:

```txt
inventory
loans
notifications
auth
users
stock
system
```

### `message`

DescripciÃ³n legible del evento tÃ©cnico.

### `related_entity`

Entidad relacionada con el evento, si aplica.

Ejemplo:

```js
related_entity: {
  entity_type: "loan",
  entity_uuid: "3ab1a4e2-54b7-47c8-8f71-8c88ab91a888"
}
```

### `error`

InformaciÃ³n del error cuando el evento representa un fallo.

### `metadata`

InformaciÃ³n adicional flexible.

Ejemplos:

```txt
- retry_count
- request_id
- job_name
- duration_ms
- source
- environment
```

### `created_at`

Fecha del evento tÃ©cnico.

### `schema_version`

VersiÃ³n del esquema.

---

## 11.5. Ejemplo de documento

```js
{
  _id: ObjectId("665f24149d3f2a0012a44e60"),

  event_type: "mongo_write_failed",
  level: "error",

  module: "inventory",

  message: "No se pudo registrar el movimiento de inventario en MongoDB despuÃ©s de confirmar la transacciÃ³n en PostgreSQL.",

  related_entity: {
    entity_type: "implement",
    entity_uuid: "b7b642ef-ec13-4d2d-bdf5-3e7d22d7a111"
  },

  error: {
    code: "MONGO_TIMEOUT",
    message: "MongoDB operation timed out",
    stack: null
  },

  metadata: {
    operation: "register_inventory_movement",
    retry_count: 1,
    request_id: "req-2026-001",
    environment: "production"
  },

  created_at: ISODate("2026-05-11T15:30:00Z"),
  schema_version: 1
}
```

---

## 11.6. Ãndices recomendados

```js
db.system_events.createIndex({ level: 1, created_at: -1 })
db.system_events.createIndex({ module: 1, created_at: -1 })
db.system_events.createIndex({ event_type: 1, created_at: -1 })
db.system_events.createIndex({ created_at: -1 })
```

---

# 12. Flujo recomendado entre PostgreSQL y MongoDB

## 12.1. Regla principal

La regla recomendada es:

```txt
Primero se confirma la operaciÃ³n principal en PostgreSQL.
DespuÃ©s se registra el evento correspondiente en MongoDB.
```

Esto evita registrar eventos histÃ³ricos de operaciones que finalmente fallaron en la base transaccional.

---

## 12.2. Ejemplo: entrega de prÃ©stamo

Cuando se entrega un prÃ©stamo, el flujo recomendado serÃ­a:

```txt
1. Validar prÃ©stamo en PostgreSQL.
2. Validar stock disponible.
3. Validar unidades individuales si corresponde.
4. Actualizar estado del prÃ©stamo en PostgreSQL.
5. Actualizar stock en PostgreSQL.
6. Actualizar estado de unidades individuales en PostgreSQL.
7. Confirmar transacciÃ³n SQL.
8. Registrar movimiento en inventory_movements.
9. Registrar evento en loan_events.
10. Crear notificaciÃ³n en notifications si corresponde.
11. Registrar system_event si ocurre algÃºn problema tÃ©cnico secundario.
```

---

## 12.3. Ejemplo de distribuciÃ³n de responsabilidades

```txt
PostgreSQL:
- loan.status = delivered
- loan.delivered_at = now()
- stock.available disminuye
- stock.loaned aumenta
- individual.status cambia a loaned

MongoDB:
- inventory_movements registra la salida
- loan_events agrega evento de cambio de estado
- notifications avisa al usuario
- system_events registra errores tÃ©cnicos si ocurren
```

---

## 12.4. QuÃ© pasa si MongoDB falla

Puede ocurrir que PostgreSQL confirme correctamente una operaciÃ³n, pero MongoDB falle al registrar el evento.

Ejemplo:

```txt
El prÃ©stamo se entregÃ³ correctamente en PostgreSQL,
pero fallÃ³ la escritura en inventory_movements.
```

En ese caso:

- No necesariamente se debe revertir la operaciÃ³n principal.
- Se debe registrar el error tÃ©cnico.
- Se puede reintentar la escritura.
- Se puede enviar el evento a una cola.
- Se puede usar un patrÃ³n Outbox a futuro.

---

## 12.5. PatrÃ³n Outbox recomendado a futuro

Si el sistema crece, se recomienda usar el patrÃ³n **Transactional Outbox**.

La idea es:

```txt
1. Durante la transacciÃ³n SQL, se guarda un evento pendiente en una tabla outbox.
2. Un proceso en segundo plano lee la outbox.
3. Ese proceso escribe el evento en MongoDB.
4. Si la escritura es exitosa, marca el evento como procesado.
5. Si falla, reintenta sin perder la operaciÃ³n original.
```

Esto mejora la confiabilidad entre PostgreSQL y MongoDB.

---

# 13. Validaciones recomendadas

## 13.1. Validar UUID

Todo campo terminado en `_uuid` debe validarse como UUID.

Ejemplos:

```txt
implement_uuid
individual_uuid
loan_uuid
loan_detail_uuid
user_uuid
performed_by_uuid
changed_by_uuid
resolved_by_uuid
```

---

## 13.2. Validar enums de dominio

No se recomienda aceptar strings completamente libres para campos importantes.

Deben validarse campos como:

```txt
action
movement_type
current_status
type
priority
alert_type
severity
event_type
level
module
```

---

## 13.3. Validar fechas

Las fechas deben guardarse como `Date` de MongoDB.

Campos comunes:

```txt
created_at
updated_at
changed_at
read_at
resolved_at
expires_at
```

---

## 13.4. Usar `schema_version`

Cada documento debe incluir:

```txt
schema_version: Number
```

Esto permite cambiar la estructura de los documentos en el futuro sin romper compatibilidad.

---

# 14. JSON Schema sugerido para `inventory_movements`

Ejemplo base de validaciÃ³n para MongoDB:

```js
db.createCollection("inventory_movements", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: [
        "implement_uuid",
        "action",
        "movement_type",
        "quantity",
        "source",
        "created_at",
        "schema_version"
      ],
      properties: {
        implement_uuid: {
          bsonType: "string",
          description: "UUID del implemento en PostgreSQL"
        },
        individual_uuid: {
          bsonType: ["string", "null"]
        },
        loan_uuid: {
          bsonType: ["string", "null"]
        },
        loan_detail_uuid: {
          bsonType: ["string", "null"]
        },
        action: {
          enum: [
            "manual_adjustment",
            "stock_entry",
            "stock_exit",
            "reserved",
            "delivered",
            "returned",
            "damaged",
            "lost",
            "stock_correction",
            "location_changed",
            "condition_changed",
            "status_changed"
          ]
        },
        movement_type: {
          enum: ["in", "out", "neutral"]
        },
        quantity: {
          bsonType: "int",
          minimum: 0
        },
        previous_stock: {
          bsonType: ["int", "null"]
        },
        new_stock: {
          bsonType: ["int", "null"]
        },
        previous_status: {
          bsonType: ["string", "null"]
        },
        new_status: {
          bsonType: ["string", "null"]
        },
        previous_condition: {
          bsonType: ["string", "null"]
        },
        new_condition: {
          bsonType: ["string", "null"]
        },
        performed_by_uuid: {
          bsonType: ["string", "null"]
        },
        source: {
          enum: ["system", "admin", "loan_flow", "stock_module", "inventory_module", "migration"]
        },
        notes: {
          bsonType: ["string", "null"]
        },
        metadata: {
          bsonType: ["object", "null"]
        },
        created_at: {
          bsonType: "date"
        },
        schema_version: {
          bsonType: "int"
        }
      }
    }
  }
})
```

---

# 15. Comandos de creaciÃ³n de Ã­ndices

```js
// inventory_movements
db.inventory_movements.createIndex({ implement_uuid: 1, created_at: -1 })
db.inventory_movements.createIndex({ individual_uuid: 1, created_at: -1 })
db.inventory_movements.createIndex({ loan_uuid: 1, created_at: -1 })
db.inventory_movements.createIndex({ performed_by_uuid: 1, created_at: -1 })
db.inventory_movements.createIndex({ created_at: -1 })

// loan_events
db.loan_events.createIndex({ loan_uuid: 1 }, { unique: true })
db.loan_events.createIndex({ requester_uuid: 1, updated_at: -1 })
db.loan_events.createIndex({ current_status: 1, updated_at: -1 })
db.loan_events.createIndex({ "status_history.changed_at": -1 })

// notifications
db.notifications.createIndex({ user_uuid: 1, read: 1, created_at: -1 })
db.notifications.createIndex({ user_uuid: 1, created_at: -1 })
db.notifications.createIndex({ related_loan_uuid: 1 }, { sparse: true })
db.notifications.createIndex({ related_implement_uuid: 1 }, { sparse: true })
db.notifications.createIndex({ expires_at: 1 }, { expireAfterSeconds: 0 })

// stock_alerts
db.stock_alerts.createIndex({ implement_uuid: 1, resolved: 1, created_at: -1 })
db.stock_alerts.createIndex({ resolved: 1, severity: 1, created_at: -1 })
db.stock_alerts.createIndex({ alert_type: 1, created_at: -1 })

// system_events
db.system_events.createIndex({ level: 1, created_at: -1 })
db.system_events.createIndex({ module: 1, created_at: -1 })
db.system_events.createIndex({ event_type: 1, created_at: -1 })
db.system_events.createIndex({ created_at: -1 })
```

---

# 16. PriorizaciÃ³n de implementaciÃ³n

No se recomienda implementar todas las colecciones al mismo tiempo.

La priorizaciÃ³n sugerida es:

## 16.1. Fase 1 â€” Trazabilidad de inventario

```txt
inventory_movements
```

Prioridad: alta.

Motivo:

```txt
Es la colecciÃ³n mÃ¡s directamente relacionada con la operaciÃ³n central del paÃ±ol.
Permite saber quÃ© pasÃ³ con el inventario, cuÃ¡ndo pasÃ³ y quiÃ©n realizÃ³ la acciÃ³n.
```

---

## 16.2. Fase 2 â€” Seguimiento de prÃ©stamos y comunicaciÃ³n

```txt
loan_events
notifications
```

Prioridad: media-alta.

Motivo:

```txt
Permiten mostrar historial de prÃ©stamos y avisar a usuarios sobre cambios relevantes.
```

---

## 16.3. Fase 3 â€” Control operativo

```txt
stock_alerts
```

Prioridad: media.

Motivo:

```txt
Permite detectar stock bajo, stock crÃ­tico o inconsistencias.
Sirve para dashboards del paÃ±olero, coordinador o director.
```

---

## 16.4. Fase 4 â€” Observabilidad tÃ©cnica

```txt
system_events
```

Prioridad: media.

Motivo:

```txt
Ayuda a diagnosticar errores tÃ©cnicos, fallos de integraciÃ³n y problemas internos del sistema.
```

---

# 17. Diagrama conceptual

```txt
                         PostgreSQL
       â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
       â”‚ user                                       â”‚
       â”‚ role                                       â”‚
       â”‚ implement                                  â”‚
       â”‚ individual                                 â”‚
       â”‚ stock                                      â”‚
       â”‚ loan                                       â”‚
       â”‚ loan_detail                                â”‚
       â”‚ loan_detail_individual                     â”‚
       â”‚ category                                   â”‚
       â”‚ location                                   â”‚
       â”‚ room                                       â”‚
       â”‚ audit_log                                  â”‚
       â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
                         â”‚
                         â”‚ referencias UUID
                         â–¼
                         MongoDB
       â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
       â”‚ inventory_movements                        â”‚
       â”‚ - implement_uuid                           â”‚
       â”‚ - individual_uuid                          â”‚
       â”‚ - loan_uuid                                â”‚
       â”‚ - loan_detail_uuid                         â”‚
       â”‚ - performed_by_uuid                        â”‚
       â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
       â”‚ loan_events                                â”‚
       â”‚ - loan_uuid                                â”‚
       â”‚ - requester_uuid                           â”‚
       â”‚ - room_uuid                                â”‚
       â”‚ - status_history[]                         â”‚
       â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
       â”‚ notifications                              â”‚
       â”‚ - user_uuid                                â”‚
       â”‚ - related_loan_uuid                        â”‚
       â”‚ - related_implement_uuid                   â”‚
       â”‚ - related_individual_uuid                  â”‚
       â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
       â”‚ stock_alerts                               â”‚
       â”‚ - implement_uuid                           â”‚
       â”‚ - stock_uuid                               â”‚
       â”‚ - resolved_by_uuid                         â”‚
       â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤
       â”‚ system_events                              â”‚
       â”‚ - related_entity.entity_uuid               â”‚
       â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
```

---

# 18. Mermaid recomendado para documentaciÃ³n

```mermaid
erDiagram

    POSTGRES_USER {
        uuid uuid PK
        string name
        string rut
        string email
        uuid role_uuid FK
    }

    POSTGRES_IMPLEMENT {
        uuid uuid PK
        string name
        string item_type
        uuid category_uuid FK
        uuid location_uuid FK
    }

    POSTGRES_INDIVIDUAL {
        uuid uuid PK
        string asset_code
        string status
        string condition
        uuid implement_uuid FK
    }

    POSTGRES_STOCK {
        uuid uuid PK
        int total_stock
        int available
        int reserved
        int loaned
        int damaged
        uuid implement_uuid FK
    }

    POSTGRES_LOAN {
        uuid uuid PK
        string status
        date scheduled_date
        time scheduled_time
        uuid requester_uuid FK
        uuid room_uuid FK
    }

    POSTGRES_LOAN_DETAIL {
        uuid uuid PK
        int requested_quantity
        int reserved_quantity
        int delivered_quantity
        uuid loan_uuid FK
        uuid implement_uuid FK
    }

    POSTGRES_AUDIT_LOG {
        uuid uuid PK
        string event
        object payload
        date created_at
        uuid actor_user_uuid FK
        uuid target_user_uuid FK
    }

    MONGO_INVENTORY_MOVEMENTS {
        objectId _id PK
        uuid implement_uuid
        uuid individual_uuid
        uuid loan_uuid
        uuid loan_detail_uuid
        string action
        string movement_type
        int quantity
        uuid performed_by_uuid
        date created_at
    }

    MONGO_LOAN_EVENTS {
        objectId _id PK
        uuid loan_uuid
        uuid requester_uuid
        uuid room_uuid
        string current_status
        array status_history
        date created_at
        date updated_at
    }

    MONGO_NOTIFICATIONS {
        objectId _id PK
        uuid user_uuid
        string type
        string title
        boolean read
        uuid related_loan_uuid
        uuid related_implement_uuid
        uuid related_individual_uuid
        date created_at
    }

    MONGO_STOCK_ALERTS {
        objectId _id PK
        uuid implement_uuid
        uuid stock_uuid
        string alert_type
        string severity
        boolean resolved
        uuid resolved_by_uuid
        date created_at
    }

    MONGO_SYSTEM_EVENTS {
        objectId _id PK
        string event_type
        string level
        string module
        object related_entity
        date created_at
    }

    POSTGRES_IMPLEMENT ||..o{ MONGO_INVENTORY_MOVEMENTS : "implement_uuid"
    POSTGRES_INDIVIDUAL ||..o{ MONGO_INVENTORY_MOVEMENTS : "individual_uuid"
    POSTGRES_LOAN ||..o{ MONGO_INVENTORY_MOVEMENTS : "loan_uuid"
    POSTGRES_LOAN_DETAIL ||..o{ MONGO_INVENTORY_MOVEMENTS : "loan_detail_uuid"
    POSTGRES_USER ||..o{ MONGO_INVENTORY_MOVEMENTS : "performed_by_uuid"

    POSTGRES_LOAN ||..|| MONGO_LOAN_EVENTS : "loan_uuid"
    POSTGRES_USER ||..o{ MONGO_LOAN_EVENTS : "requester_uuid"

    POSTGRES_USER ||..o{ MONGO_NOTIFICATIONS : "user_uuid"
    POSTGRES_LOAN ||..o{ MONGO_NOTIFICATIONS : "related_loan_uuid"
    POSTGRES_IMPLEMENT ||..o{ MONGO_NOTIFICATIONS : "related_implement_uuid"
    POSTGRES_INDIVIDUAL ||..o{ MONGO_NOTIFICATIONS : "related_individual_uuid"

    POSTGRES_IMPLEMENT ||..o{ MONGO_STOCK_ALERTS : "implement_uuid"
    POSTGRES_STOCK ||..o{ MONGO_STOCK_ALERTS : "stock_uuid"
    POSTGRES_USER ||..o{ MONGO_STOCK_ALERTS : "resolved_by_uuid"

    POSTGRES_USER ||..o{ POSTGRES_AUDIT_LOG : "actor_user_uuid"
    POSTGRES_USER ||..o{ POSTGRES_AUDIT_LOG : "target_user_uuid"
```

---

# 19. Resumen final

El diseÃ±o recomendado para MongoDB queda limitado a cinco colecciones:

```txt
MongoDB
â”œâ”€â”€ inventory_movements
â”œâ”€â”€ loan_events
â”œâ”€â”€ notifications
â”œâ”€â”€ stock_alerts
â””â”€â”€ system_events
```

La colecciÃ³n `audit_logs` no se incluye en MongoDB porque la auditorÃ­a formal ya existe en PostgreSQL mediante `audit_log`.

La separaciÃ³n final queda asÃ­:

```txt
PostgreSQL:
- Estado actual.
- Entidades principales.
- Relaciones.
- Reglas de negocio.
- Transacciones.
- AuditorÃ­a formal.

MongoDB:
- Historial de movimientos.
- Timeline de prÃ©stamos.
- Notificaciones.
- Alertas de stock.
- Eventos tÃ©cnicos internos.
```

Esta separaciÃ³n es mÃ¡s limpia, evita duplicar responsabilidades y mantiene MongoDB enfocado en lo que realmente aporta al sistema: flexibilidad, trazabilidad operativa, eventos histÃ³ricos y observabilidad tÃ©cnica.



