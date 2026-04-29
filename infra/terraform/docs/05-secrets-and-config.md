# 05 - Secrets y Configuración por Entorno

## Principio de diseño

- Terraform crea infraestructura y contenedores de secretos.
- Valores sensibles viven en GitHub Secrets y/o Secret Manager.
- No se guardan secretos en el repositorio.

## Secrets de GitHub (dev)

- `GCP_WIF_PROVIDER_DEV`
- `GCP_SERVICE_ACCOUNT_DEV`
- `DB_SUPABASE_PASSWORD_DEV`
- `JWT_ISSUER_URI_DEV`
- `VITE_SUPABASE_PUBLISHABLE_KEY_DEV`

## Variables de GitHub (dev)

- `GCP_PROJECT_ID_DEV`
- `GCP_REGION_DEV`
- `GCP_ARTIFACT_REGISTRY_LOCATION_DEV`
- `GCP_TFSTATE_BUCKET_DEV`
- `SUPABASE_DB_HOST_DEV`
- `SUPABASE_DB_PORT_DEV`
- `SUPABASE_DB_NAME_DEV`
- `SUPABASE_DB_USER_DEV`

## Equivalente en prod

Duplicar estructura con sufijo `_PROD`.

## Secretos en GCP Secret Manager

Contenedores esperados:

- `DB_SUPABASE_PASSWORD`
- `JWT_ISSUER_URI`
- `VITE_SUPABASE_PUBLISHABLE_KEY`

## Rotación

- Manual desde workflow (`rotate_secrets=true`) o por comando `gcloud`.
- Mantener inventario de versiones activas.

## Consideraciones de seguridad

- Aplicar principio de mínimo privilegio en SA de deploy y runtime.
- Evitar permisos editor/owner para workflows.
- Revisar periódicamente IAM bindings y uso de secretos.
