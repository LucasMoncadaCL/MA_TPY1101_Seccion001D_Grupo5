# Modulo: catalog/location

- Estado del documento: vigente
- Ultima verificacion: 2026-05-15
- Fuente de verdad: `LocationV2Controller`, `LocationService`

## Responsabilidad

Gestion de ubicaciones del catalogo y validaciones de activacion para consumo de otros modulos.

## API vigente

Base path: `/api/v2/locations`

- `GET /`
- `GET /management`
- `POST /`
- `PUT /{locationUuid}`
- `PATCH /{locationUuid}/active`

## Notas

- IDs publicos UUID.
- Validaciones y errores con `code` estable.
- Contratos de validacion para consumo cross-modulo.

