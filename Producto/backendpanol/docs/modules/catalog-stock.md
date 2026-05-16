# Modulo: catalog/stock

- Estado del documento: vigente
- Ultima verificacion: 2026-05-15
- Fuente de verdad: `StockV2Controller`, `InventoryMovementV2Controller`, `BarcodeLabelV2Controller`

## Responsabilidad

Gestion de stock por implemento, movimientos de inventario y generacion de etiquetas.

## API vigente

- `GET /api/v2/implements/{implementUuid}/stock`
- `POST /api/v2/implements/{implementUuid}/stock/entries`
- `POST /api/v2/implements/{implementUuid}/stock/movements`
- `PUT /api/v2/implements/{implementUuid}/stock/individuals/{individualUuid}`
- `GET /api/v2/implements/movements`
- `POST /api/v2/implements/{implementUuid}/movements`
- `GET /api/v2/implements/{implementUuid}/labels/pdf`

## Fronteras

- Colaboracion con `implement` via contratos cross-modulo.
- Sin dependencia a API de otros modulos.
- Emision de eventos de stock/movimientos via outbox.

