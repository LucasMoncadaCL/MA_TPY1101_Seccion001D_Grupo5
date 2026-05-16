- Estado del documento: histórico-vigente
- Última verificación: 2026-05-16
- Fuente de verdad: `V8__uuid_phase_a_base_columns.sql`

# V8__uuid_phase_a_base_columns.sql

## Motivo
Comenzar corte a UUID: agregar columna `uuid` por tabla (con default y unicidad).

## Justificación
Migrar de identificadores numéricos a UUID para compatibilidad con nuevos servicios y evitar conflictos de clave en integraciones.

## Cómo se llegó a esta conclusión
- Fallos operativos y warnings mostraban uso mixto de `id` y `uuid`.
- Se adoptó fase progresiva: habilitar primero columnas UUID sin romper esquema existente.

## Impacto operativo
- **Módulos:** todos los módulos persistentes (`users`, `category`, `location`, `implement`, `stock`, `loan`, `individual`, etc.).
- **Endpoints `v2`:** preparación para consumir UUID de forma estable.

## Estado
Vigente como fase histórica del corte UUID.
