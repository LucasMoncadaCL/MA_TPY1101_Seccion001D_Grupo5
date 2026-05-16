# Gestion de Datos: Guia Operativa

- Estado del documento: vigente
- Ultima verificacion: 2026-05-15
- Fuente de verdad: migraciones Flyway + outbox actual + modulos backend vigentes

## Indice y estado

1. [01-flujo-end-to-end.md](./01-flujo-end-to-end.md) - vigente
2. [02-responsabilidades-por-capa.md](./02-responsabilidades-por-capa.md) - vigente
3. [03-postgresql-guia-tecnica.md](./03-postgresql-guia-tecnica.md) - vigente
4. [04-mongodb-guia-tecnica.md](./04-mongodb-guia-tecnica.md) - historico
5. [05-backend-integracion-datos.md](./05-backend-integracion-datos.md) - vigente
6. [06-runbook-operacional-datos.md](./06-runbook-operacional-datos.md) - vigente
7. [07-aligerar-sql-prestamos.md](./07-aligerar-sql-prestamos.md) - historico
8. [08-flujo-migraciones-flyway.md](./08-flujo-migraciones-flyway.md) - vigente
9. [09-uuid-cutover-backup-rollback.md](./09-uuid-cutover-backup-rollback.md) - historico
10. [10-inventario-deuda-id-numerico.md](./10-inventario-deuda-id-numerico.md) - historico
11. [11-corte-uuid-only-plan-y-estado.md](./11-corte-uuid-only-plan-y-estado.md) - historico
12. [12-corte-final-uuid-only-checklist.md](./12-corte-final-uuid-only-checklist.md) - historico
13. [13-ejecucion-corte-final-uuid-only.md](./13-ejecucion-corte-final-uuid-only.md) - historico
14. [14-deuda-tecnica-uuid-cierre.md](./14-deuda-tecnica-uuid-cierre.md) - historico
15. [15-outbox-flujo-completo.md](./15-outbox-flujo-completo.md) - vigente

## Convenciones

- SQL es la fuente canonica de estado transaccional.
- Outbox asegura entrega eventual de eventos.
- Mongo se usa para proyecciones, historicos operativos y observabilidad funcional.

