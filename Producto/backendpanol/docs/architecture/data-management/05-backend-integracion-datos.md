# 05 - Backend: Integración con Bases de Datos

## 1. Patrón de servicio

Cada caso de uso backend debe:

1. Validar input y permisos.
2. Persistir estado canónico en PostgreSQL.
3. Confirmar transacción.
4. Publicar/registrar evento en MongoDB.
5. Devolver respuesta API.

## 2. Estrategia de escritura

### Síncrona (actual)
- SQL primero, luego Mongo.
- Si Mongo falla, registrar error y reintentar.

### Recomendada (evolución)
- SQL + Outbox en misma transacción.
- Worker asíncrono procesa outbox -> Mongo.
- Garantiza entrega eventual con idempotencia.

## 3. Idempotencia

- Para eventos, usar `event_id` único.
- En Mongo, `upsert` por clave natural (`loan_id` + timestamp/event_id) según colección.
- Evitar duplicados en reprocesos.

## 4. Contratos API y agregación

- Endpoints operativos: basados en SQL.
- Endpoints de timeline/auditoría/notificaciones: basados en Mongo.
- Endpoints de dashboard: servicio agregador en backend que combina ambas fuentes.

## 5. Errores y observabilidad

- Trazas con `request_id`.
- Métricas por operación:
  - tiempo SQL,
  - tiempo Mongo,
  - fallas por capa.
- Logs estructurados con contexto de entidad (`entity_type`, `entity_id`).

## 6. Tests recomendados

- Unit tests de casos de uso (reglas de negocio).
- Integración SQL (transacciones, constraints).
- Integración Mongo (insert/update de eventos).
- Test de contrato para endpoints de dashboard.
