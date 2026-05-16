- Estado del documento: vigente
- Última verificación: 2026-05-16
- Fuente de verdad: `V3__implement_observations_column.sql`

# V3__implement_observations_column.sql

## Motivo
Agregar campo `observations` opcional a `implement`.

## Justificación
Se necesitó registrar observaciones de alta sin afectar la validación obligatoria de campos principales.

## Cómo se llegó a esta conclusión
- HU funcionales de implementación requerían metadatos adicionales.
- Se optó por nullable para no romper escrituras históricas.

## Impacto operativo
- **Módulos:** `catalog.implement`.
- **Endpoints `v2`:** `POST/PUT /api/v2/implements` aceptan observación sin cambiar contrato principal.

## Estado
Vigente.
