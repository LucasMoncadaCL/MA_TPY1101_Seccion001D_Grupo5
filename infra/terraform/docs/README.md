# Infraestructura y Deploy (Índice)

Este directorio documenta de forma integral la infraestructura Terraform y el despliegue automático hacia GCP.

## Documentos

1. [01-architecture.md](./01-architecture.md)
   - Arquitectura de alto nivel, componentes y flujo de datos.
2. [02-terraform-structure.md](./02-terraform-structure.md)
   - Estructura de carpetas, módulos y contratos de variables/outputs.
3. [03-gcp-bootstrap.md](./03-gcp-bootstrap.md)
   - Preparación de GCP: proyectos, APIs, bucket tfstate, IAM y WIF.
4. [04-github-actions-deploy.md](./04-github-actions-deploy.md)
   - Funcionamiento detallado del workflow de deploy y su estrategia.
5. [05-secrets-and-config.md](./05-secrets-and-config.md)
   - Modelo de secretos, variables y gestión por entorno.
6. [06-operations-runbook.md](./06-operations-runbook.md)
   - Operación diaria: plan/apply, promoción, rollback y validaciones.
7. [07-cost-optimization.md](./07-cost-optimization.md)
   - Costos, tradeoffs y controles para minimizar gasto.
8. [08-troubleshooting.md](./08-troubleshooting.md)
   - Fallas comunes y resolución paso a paso.
9. [09-env-vars-and-secrets-guide.md](./09-env-vars-and-secrets-guide.md)
   - Guía práctica para agregar variables/secrets de forma consistente y resiliente.

## Alcance actual

- Runtime: Cloud Run.
- Imágenes: Artifact Registry.
- Estado Terraform: GCS bucket remoto.
- Secretos runtime: Secret Manager.
- Integración CI/CD: GitHub Actions + Workload Identity Federation (OIDC).
- Base de datos: Supabase externa (no provisionada por Terraform).

## Estado de ambientes

- `dev`: despliegue automático en push a rama `dev`.
- `prod`: despliegue manual por `workflow_dispatch`.
- Dominio custom: desactivado por defecto (`backend_domain` y `frontend_domain` vacíos), usando URLs `*.run.app`.
