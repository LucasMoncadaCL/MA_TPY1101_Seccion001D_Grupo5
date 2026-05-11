# UUID-Only Big-Bang Release Checklist

## Objetivo
Cerrar deuda legacy y ejecutar corte final UUID-only con gate estricto de calidad.

## Gate obligatorio antes de corte DB
1. Ejecutar dos corridas consecutivas:

```powershell
node Producto/backendpanol/scripts/qa/run-v2-smoke.mjs --env-file Producto/backendpanol/docs/qa/.env.qa
node Producto/backendpanol/scripts/qa/run-v2-smoke.mjs --env-file Producto/backendpanol/docs/qa/.env.qa
```

2. Validar gate:

```powershell
node Producto/backendpanol/scripts/qa/validate-two-green-runs.mjs --reports-root Producto/backendpanol/docs/qa/reports --required-greens 2
```

Si el gate falla, NO ejecutar corte DB.

## Secuencia de ejecución
1. Respaldos PostgreSQL + Mongo + plan de rollback validado.
2. `uuid-precheck.sql`.
3. Gate QA de 2 corridas verdes consecutivas.
4. Ejecutar `uuid-final-cutover.sql`.
5. Desplegar backend + frontend final.
6. Ejecutar `uuid-postcheck.sql`.
7. Repetir smoke v2 y correlación Cloud Run.

## Evidencia mínima de cierre
- IDs de las dos corridas verdes (`reports/<RUN_ID>`).
- Salida PASS de `validate-two-green-runs.mjs`.
- Logs Cloud Run sin `500` para ventana post-deploy.
- Confirmación de bloqueo `/api/v1/**` y rutas legacy.
