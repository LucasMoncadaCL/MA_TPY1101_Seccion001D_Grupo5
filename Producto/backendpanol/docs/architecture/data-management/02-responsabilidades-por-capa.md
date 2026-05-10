# 02 - Responsabilidades por Capa

## Backend (Spring Boot)

Responsable de:
- Autenticación/autorización.
- Validaciones de negocio.
- Orquestación transaccional entre SQL y Mongo.
- Exposición de contratos API.
- Manejo de errores y observabilidad.

No debe:
- Duplicar reglas que pertenecen a constraints críticas de SQL.
- Delegar decisiones de negocio en triggers complejos sin trazabilidad.

## PostgreSQL (Relacional)

Responsable de:
- Entidades maestras y relaciones.
- Integridad referencial (FK).
- Consistencia transaccional ACID.
- Restricciones de dominio (CHECK, UNIQUE, NOT NULL).
- Vistas/consultas agregadas críticas.

No debe:
- Almacenar trazabilidad de alta cardinalidad orientada a eventos si impacta rendimiento OLTP.

## MongoDB (No relacional)

Responsable de:
- Eventos append-only.
- Historial cronológico por entidad.
- Notificaciones y alertas operativas.
- Auditoría enriquecida (`before/after/meta`).

No debe:
- Reemplazar tablas maestras SQL.
- Ser la fuente de verdad de estados transaccionales críticos.

## Tabla de decisión rápida

- ¿Requiere FK/ACID fuerte? -> PostgreSQL.
- ¿Es timeline/evento/auditoría/notificación? -> MongoDB.
- ¿Es cálculo cross-domain para UI? -> Backend (servicio agregador).
