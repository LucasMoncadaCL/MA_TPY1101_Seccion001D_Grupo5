# 03 - PostgreSQL: Guía Técnica

## 1. Objetivo

PostgreSQL mantiene el estado maestro del sistema: usuarios, roles, inventario, stock, préstamos y relaciones.

## 2. Diseño recomendado

- Esquema `public` (actual) o partición por dominios a futuro (`auth`, `inventory`, `loan`).
- Migraciones con Flyway (`Vn__*.sql`, solo forward-only).
- Constraints como primera línea de defensa de consistencia.

## 3. Índices mínimos por dominio

### Auth / Usuarios
- `user(rut)` UNIQUE
- `user(email)` UNIQUE
- `user(active)` INDEX
- `user(role_id)` INDEX

### Inventario
- `implement(active)` INDEX
- `implement(category_id)` INDEX
- `stock(implement_id)` UNIQUE
- Índice compuesto sugerido: `stock(available, min_stock)` para alertas de bajo stock.

### Préstamos
- `loan(status)` INDEX
- `loan(due_date)` INDEX parcial `WHERE due_date IS NOT NULL`
- `loan(requester_id)` INDEX
- `loan_detail(loan_id)` INDEX
- `loan_detail(implement_id)` INDEX

## 4. Triggers sugeridos

### 4.1 Trigger de timestamp
- `BEFORE UPDATE` en tablas maestras para actualizar `updated_at`.

### 4.2 Trigger de sincronía de stock
- Trigger para validar invariantes si cambian `available/reserved/loaned/damaged`.

### 4.3 Trigger de outbox (recomendado)
- `AFTER INSERT/UPDATE` en `loan`, `stock`, `implement` para insertar evento en tabla `outbox_events`.

## 5. Procedures / Functions sugeridas

Nota: en PostgreSQL se usan **functions/procedures**, no "packages" como Oracle.

### 5.1 `fn_apply_stock_movement(...)`
- Aplica ingreso/egreso/ajuste.
- Valida no negativos.
- Retorna estado actualizado.

### 5.2 `fn_change_loan_status(...)`
- Controla transición de estado permitida.
- Registra metadata de cambio.

### 5.3 `fn_upsert_user_role(...)`
- Normaliza cambios de rol con validaciones centralizadas.

## 6. Vistas recomendadas

- `v_stock_summary` (ya existe) para panel operativo.
- `v_active_loans` (ya existe) para seguimiento.
- Vista sugerida `v_dashboard_inventory_snapshot` con métricas ejecutivas agregadas.

## 7. Mantenimiento y performance

- `VACUUM (ANALYZE)` programado.
- Monitor de consultas lentas (`pg_stat_statements`).
- Revisión trimestral de índices no usados.
- Tests de regresión SQL en CI para migraciones.

## 8. Seguridad

- Menor privilegio por usuario de aplicación.
- Evitar uso de superuser en runtime.
- Secrets por `application-secrets.properties` / entorno.
