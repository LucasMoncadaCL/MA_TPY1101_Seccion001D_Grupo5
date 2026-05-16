- Estado del documento: histórico-vigente
- Última verificación: 2026-05-16
- Fuente de verdad: `V13__uuid_only_post_checks.sql`

# V13__uuid_only_post_checks.sql

## Motivo
Definir vistas de validación post-corte para detectar nulos, nulos por UUID y órfanos FK.

## Justificación
Necesario verificar que la migración no dejara datos inconsistentes antes de cerrar contratos.

## Cómo se llegó a esta conclusión
- Corte incremental sin checks explícitos no garantiza calidad de datos entre módulos.
- Se priorizó observabilidad técnica con vistas de `pending`.

## Impacto operativo
- **Módulos:** todos.
- **Endpoints `v2`:** reduce riesgo de errores intermitentes al consumir UUID.

## Estado
Histórico.
