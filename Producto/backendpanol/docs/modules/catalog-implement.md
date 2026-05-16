# Modulo: catalog/implement

- Estado del documento: vigente
- Ultima verificacion: 2026-05-15
- Fuente de verdad: `ImplementV2Controller`, contratos `application.contract`, `ArchitectureTest`

## Responsabilidad

Gestion del catalogo de implementos y su integracion con categoria, ubicacion y vistas de movimientos recientes por contratos cross-modulo.

## API vigente

Base path: `/api/v2/implements`

- `GET /`
- `GET /{implementUuid}`
- `POST /`
- `PUT /{implementUuid}`
- `PATCH /{implementUuid}/active`

Relacionadas:
- `GET /api/v2/implements/movements`
- `POST /api/v2/implements/{implementUuid}/movements`

## Contratos y acoplamiento

- Dependencias cross-modulo via `application.contract`.
- No dependencia a API de otros modulos.
- DTO HTTP propio de modulo `implement`.

## Validaciones relevantes

- Categoria y ubicacion validadas por contrato.
- Reglas de nombre y estado activo/inactivo.
- Respuesta de error con `code` estable.

