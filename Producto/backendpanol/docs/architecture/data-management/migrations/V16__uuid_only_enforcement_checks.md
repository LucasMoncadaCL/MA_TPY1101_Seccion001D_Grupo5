- Estado del documento: histórica (bloque de guardia)
- Última verificación: 2026-05-16
- Fuente de verdad: `V16__uuid_only_enforcement_checks.sql`

# V16__uuid_only_enforcement_checks.sql

## Motivo
Fallar fast si persiste cualquier columna legacy `id`/`*_id` en tablas de dominio SQL.

## Justificación
Evita que un despliegue incompleto deje una mezcla de identificadores y vuelva a introducir regresiones.

## Cómo se llegó a esta conclusión
- Después del corte inicial era necesario convertir el objetivo en exigible, no solo aspiracional.
- Esta migración convierte validación en control de instalación.

## Impacto operativo
- **Módulos:** todos.
- **Endpoints `v2`:** protege al runtime de errores estructurales y rompe temprano en caso de drift de esquema.

## Estado
Histórico de enforcement en ruta de corte.
