# 01 - Arquitectura de Infraestructura

## Objetivo

Desplegar backend y frontend del proyecto en GCP con IaC, separando responsabilidades de infraestructura, secretos y pipeline.

## Componentes principales

- **Cloud Run (backend)**: ejecuta la API Java/Spring Boot.
- **Cloud Run (frontend)**: ejecuta el frontend estático servido por Nginx.
- **Artifact Registry**: almacena imágenes Docker versionadas.
- **Secret Manager**: almacena secretos sensibles del runtime.
- **GCS (tfstate)**: backend remoto de Terraform.
- **IAM + Workload Identity Federation**: autenticación segura GitHub -> GCP sin llaves JSON.

## Flujo general

1. Desarrollador hace push a `dev`.
2. GitHub Actions autentica con GCP usando OIDC/WIF.
3. Workflow construye imágenes backend/frontend.
4. Workflow publica imágenes en Artifact Registry.
5. Terraform aplica cambios en Cloud Run usando imágenes nuevas.
6. Cloud Run expone URLs `run.app`.

## Diagrama lógico (simplificado)

```text
GitHub Repo
  └─ GitHub Actions (deploy-gcp.yml)
      ├─ OIDC -> Workload Identity Provider (GCP)
      ├─ Impersonate Service Account (deploy)
      ├─ docker build/push -> Artifact Registry
      └─ terraform apply -> Cloud Run + Secret Manager + IAM

Cloud Run (frontend) ---> Cloud Run (backend) ---> Supabase (externo)
```

## Principios aplicados

- **Sin secretos hardcodeados** en código o tfvars.
- **IaC modular** por ambientes (`dev`/`prod`) con módulos reutilizables.
- **Menor superficie de credenciales** (OIDC en vez de keys JSON).
- **Costo optimizado en dev** con scale-to-zero.
