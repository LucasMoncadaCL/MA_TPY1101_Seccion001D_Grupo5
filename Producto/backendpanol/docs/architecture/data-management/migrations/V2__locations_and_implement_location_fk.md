- Estado del documento: vigente
- Última verificación: 2026-05-16
- Fuente de verdad: `V2__locations_and_implement_location_fk.sql`

# V2__locations_and_implement_location_fk.sql

## Motivo
Agregar entidad `location` y asociarla obligatoriamente al `implement`.

## Justificación
Los flujos de inventario requerían ubicación consistente por implemento para filtrar movimientos y reportes.

## Cómo se llegó a esta conclusión
- Se observó dependencia de ubicación en reglas de negocio de catálogo e inventario.
- `implement.location_id` no existía en versiones tempranas o no era obligatorio.

## Impacto operativo
- **Módulos:** `catalog.location`, `catalog.implement`.
- **EndPoints `v2`:** creaciones/actualizaciones de implementos y listados por ubicación dependen de esta FK.

## Estado
Vigente.

## Alineación con cambios de endpoints recientes
Apoya validaciones actuales de crear/editar implemento y ubicación con referencias estables para front y API.
