# 04 - GitHub Actions: Deploy de Infraestructura

## Workflow principal

Archivo:

- `.github/workflows/deploy-gcp.yml`

## Triggers

- `push` a rama `dev` con cambios relevantes (infra/backend/frontend/workflow).
- `workflow_dispatch` manual con input:
  - `environment` (`dev` | `prod`)
  - `rotate_secrets` (`true` | `false`)

## Seguridad de autenticación

- `permissions: id-token: write`
- `google-github-actions/auth@v2` con WIF provider + service account

No se usan llaves JSON almacenadas en GitHub.

## Flujo job `deploy-dev`

1. Checkout
2. Auth GCP por WIF
3. Setup gcloud + terraform
4. Cálculo de tags de imágenes
5. `terraform init` con backend bucket `GCP_TFSTATE_BUCKET_DEV`
6. Bootstrap targeted (`artifact_registry`, `secret_manager`, `runtime_iam`)
7. (Opcional) rotación de secretos si `rotate_secrets=true`
8. Build backend/frontend
9. Push imágenes a Artifact Registry
10. `terraform apply` completo (Cloud Run actualizado)

## Flujo job `deploy-prod`

- Solo manual (`workflow_dispatch` con `environment=prod`).
- Misma estructura que dev, con variables/secrets de prod.

## Control de costos y estabilidad

- `concurrency.cancel-in-progress=true` para evitar pipelines redundantes.
- Rotación de secretos desactivada por defecto en push.

## Requisitos en GitHub

- Environment `dev` y `prod` creados.
- Branch restriction recomendada:
  - `dev` env -> rama `dev`
  - `prod` env -> rama `main`
- Secrets y variables definidos correctamente (ver docs 05).
