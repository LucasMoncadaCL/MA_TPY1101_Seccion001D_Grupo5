# 09 - Guía de Variables de Entorno y Secretos

Esta guía define el proceso estándar para agregar una nueva configuración al sistema sin romper deploys.

## Objetivo

- Mantener consistencia entre Terraform, GitHub Actions y Cloud Run.
- Evitar errores comunes: secreto sin versión, nombre inconsistente, valor faltante en CI/CD.
- Reducir riesgo de exposición de secretos.

## Regla principal: ¿`env var` o `secret`?

- Usa `env var` normal si el dato no es sensible (feature flags, puertos, timeouts, URLs públicas).
- Usa `Secret Manager` si el dato es sensible (passwords, URIs con credenciales, API keys, tokens).

## Estándar de nombres

- `APP_*` para configuración de app no sensible.
- `VITE_*` para variables necesarias en frontend (si es sensible, sigue en Secret Manager).
- Secretos en mayúsculas con `_`: ejemplo `MONGODB_URI`, `JWT_ISSUER_URI`.
- Sufijo por entorno solo en GitHub (`*_DEV`, `*_PROD`), no en el nombre del secreto en GCP.

## Flujo para agregar una nueva `env var` no sensible

1. Agregar variable a `locals.backend_env` o `locals.frontend_env` en:
   - `infra/terraform/environments/dev/main.tf`
   - `infra/terraform/environments/prod/main.tf`
2. Agregar variable de entrada en:
   - `infra/terraform/environments/dev/variables.tf`
   - `infra/terraform/environments/prod/variables.tf`
3. Mapear `TF_VAR_*` en workflow si el valor viene desde GitHub.
4. Validar con `terraform plan` y luego `terraform apply`.

## Flujo para agregar un nuevo secreto

1. Registrar el secreto en Terraform (contenedor del secreto):
   - Agregar nombre a `module "secret_manager".secrets` en `dev/main.tf` y `prod/main.tf`.
2. Referenciarlo en Cloud Run:
   - Agregar en `secret_env_vars` del servicio correspondiente (`backend_service` o `frontend_service`) con `version = "latest"`.
3. Crear GitHub Secret por entorno:
   - `NUEVO_SECRETO_DEV`
   - `NUEVO_SECRETO_PROD` (si aplica).
4. Actualizar workflow `.github/workflows/deploy-gcp.yml`:
   - En `Ensure secret versions exist`, agregar:
     - variable de entorno con `${{ secrets.NUEVO_SECRETO_DEV }}`
     - verificación `gcloud secrets versions list ...`
     - creación condicional `gcloud secrets versions add ...` si no existe versión habilitada.
   - En `Rotate secret versions`, agregar creación explícita de nueva versión.
5. Ejecutar deploy en `dev` y validar que exista al menos una versión habilitada.

## Checklist anti-errores (obligatorio)

1. Nombre del secreto idéntico en:
   - Terraform `secrets`
   - `secret_env_vars.secret`
   - comandos `gcloud` del workflow.
2. Existe GitHub Secret del entorno (`*_DEV` / `*_PROD`) con valor no vacío.
3. Existe al menos una versión habilitada en Secret Manager.
4. El service account runtime tiene `roles/secretmanager.secretAccessor`.
5. No usar `-target` en apply normal (solo recuperación puntual).

## Verificación rápida post-deploy

1. Confirmar versión del secreto:
   - `gcloud secrets versions list NOMBRE_SECRETO --project <project-id>`
2. Confirmar revisión desplegada en Cloud Run:
   - `gcloud run services describe <service> --region <region> --project <project-id>`
3. Validar logs sin error de secreto faltante:
   - `gcloud run services logs read <service> --region <region> --project <project-id>`

## Falla común y solución

- Error: `.../secrets/<SECRET>/versions/latest was not found`
  - Causa: secreto existe pero no tiene versión habilitada.
  - Solución: agregar versión con `gcloud secrets versions add` y redeploy.

## Recomendación de seguridad

- Mantener valores sensibles fuera de `terraform state`.
- Preferir rotación de versiones mediante GitHub Secrets + `gcloud` en workflow.
- Limitar acceso a environments (`dev`, `prod`) con reglas de aprobación/restricción de rama.
