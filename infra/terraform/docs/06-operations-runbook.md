# 06 - Operación y Runbook Diario

## Flujo diario en dev

1. Commit/push a `dev`.
2. GitHub Actions despliega automáticamente.
3. Validar outputs de job y URL `run.app`.
4. Probar endpoints críticos (health, endpoints negocio).

## Promoción a prod

1. Confirmar cambios validados en dev.
2. Ejecutar workflow manual `Deploy GCP` con `environment=prod`.
3. Monitorear métricas de error/latencia y logs de arranque.

## Rollback rápido

Opción recomendada:

- redeploy con imagen anterior (`backend_image` / `frontend_image` tag previo) y `terraform apply`.

## Validaciones post deploy

- Cloud Run revisions: revision activa esperada.
- Health endpoint backend responde.
- Frontend carga y apunta a backend correcto.
- Secret env vars presentes.

## Cambios en variables de infraestructura

Cuando cambie una variable de configuración:

1. actualizar GitHub Environment var/secret
2. ejecutar deploy (push o manual)
3. verificar que la revision use los nuevos valores

## Operación sin dominio custom

Con `backend_domain=""` y `frontend_domain=""`:

- se usan URLs nativas `run.app`.
- no hay dependencia de DNS externo.
- costo operativo más bajo y configuración más simple.
