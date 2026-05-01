# Documentación de Cambios: HU-19 — Inventario y Trazabilidad

**ID de Tarea:** PSD-24
**Descripción:** Como Coordinador de Laboratorio, Director de Carrera y Docente, quiero ver la ficha completa de un producto con sus atributos, stock por estado e historial de últimos movimientos para mantener el control y trazabilidad del inventario.

---

## Resumen de la Implementación

Se ha implementado una solución de arquitectura híbrida (Políglota) utilizando **PostgreSQL** para datos relacionales (productos, usuarios, categorías) y **MongoDB** para el historial de movimientos de inventario (datos de series temporales/eventos). Se realizaron integraciones a nivel de aplicación para consolidar la información en una vista única y coherente.

---

## Subtareas Realizadas

### Subtarea 1 — Extender Endpoint GET /api/implements/{id} (Backend)
- Se actualizó el modelo de respuesta del catálogo para incluir información detallada de stock.
- Integración con el `StockService` existente para calcular disponibilidades en tiempo real.
- Diferenciación de visualización de stock según permisos del usuario.

### Subtarea 2 — Definición de Colección MongoDB `inventory_movement`
- Configuración de la infraestructura de persistencia con `spring-boot-starter-data-mongodb`.
- Creación del modelo de dominio `InventoryMovement` con campos:
    - `implement_id` (Integer)
    - `action` (Enum: INGRESO, AJUSTE, RESERVA, etc.)
    - `quantity` (Integer)
    - `performed_by` (Integer - ID de usuario en PostgreSQL)
    - `timestamp` (Instant)
    - `notes` (String)

### Subtarea 3 — Registro de Movimientos y Repositorio
- Creación de `InventoryMovementRepository` extendiendo `MongoRepository`.
- Implementación de `InventoryMovementService` para persistir eventos de inventario.
- Exposición de endpoint `POST /api/implements/{id}/movements` restringido a tipos manuales (`INGRESO`, `AJUSTE`) para mantener la integridad del negocio en el Sprint 1.

### Subtarea 4 — Integración de Movimientos y JOIN Aplicativo
- Extensión del endpoint principal del producto para incluir el array `recent_movements` (últimos 10 registros).
- **JOIN en Memoria:** Implementación de `UserService` y `UserJooqRepository` para resolver los nombres de los usuarios desde PostgreSQL basándose en los IDs almacenados en MongoDB.
- Optimización de consultas relacionales mediante el operador `IN` para evitar el problema de consultas N+1.

### Subtarea 5 — Vista de Ficha Completa (Frontend)
- **Refactorización de Autenticación:** Creación de `src/utils/auth.ts` para centralizar la gestión de roles (`UserRole`).
- **Vista Dinámica por Rol:**
    - **Coordinador/Director:** Acceso total a KPIs de stock por color, tabla de unidades individuales y tabla de historial de movimientos.
    - **Docente:** Vista simplificada. Se ocultan datos sensibles de stock detallado y el historial, mostrando únicamente disponibilidad textual mediante el campo `available_display`.
- **UI/UX:**
    - Implementación de badges para categorías inactivas.
    - Formateo de fechas localizadas.
    - Manejo de estados de carga y errores de red.

---

## Tecnologías Utilizadas
- **Backend:** Java 21, Spring Boot 3.3, jOOQ (PostgreSQL), Spring Data MongoDB.
- **Frontend:** React, Vite, TypeScript, Vanilla CSS (Design System propio).
- **Infraestructura:** Docker Compose (Postgres + MongoDB + Backend + Frontend).

---

## Verificación
- Pruebas de integración manuales verificando la consistencia de datos entre ambas bases de datos.
- Validación de flujo de seguridad simulando roles de Docente y Coordinador.
- Compilación exitosa del bundle de producción del frontend.
