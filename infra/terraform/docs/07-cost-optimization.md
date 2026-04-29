# 07 - Optimización de Costos

## Fuentes de costo principales

- Cloud Run (CPU/RAM por request o instancia mínima)
- Artifact Registry (almacenamiento de imágenes)
- Secret Manager (secretos/versiones y operaciones)
- GCS tfstate (almacenamiento + operaciones)
- Egress de red

## Ajustes aplicados

- Dev con `min_instance_count=0` (scale-to-zero).
- Concurrency y timeout moderados en dev.
- Cancelación de pipelines redundantes.
- Rotación de secretos no automática en cada push.

## Recomendaciones adicionales

1. **Retention de imágenes** en Artifact Registry:
   - conservar últimas N imágenes por servicio.
2. Reducir frecuencia de deploy en dev:
   - agrupar commits cuando sea posible.
3. Evitar `min_instance_count > 0` en dev.
4. Medir cold starts vs costo antes de subir mínimos en prod.

## Política sugerida por entorno

- `dev`: costo mínimo (min=0, max bajo, sin dominio custom)
- `prod`: balance costo/latencia (definir min instances según SLA)

## Señales para revisar costo

- aumento rápido de storage en registry
- gran número de versiones de secretos
- picos de egress no esperados
- muchos deploys redundantes por commit frecuente
