# QA Investigación Endpoints v2

## Objetivo
Ejecutar una investigación completa de la API v2 para:
- Validar smoke/CRUD y negativos clave.
- Detectar errores 500 (especialmente en `POST /api/v2/implements`).
- Generar evidencia y un plan de corrección priorizado.

## 1) Preparar entorno
1. Copiar `Producto/backendpanol/docs/qa/.env.qa.example` a `Producto/backendpanol/docs/qa/.env.qa`.
2. Completar `QA_BASE_URL` con la URL del backend (no frontend).
3. Completar credenciales reales de `COORDINADOR` y `DIRECTOR`.

## 2) Ejecutar suite automatizada
Desde la raíz del repositorio:

```powershell
node Producto/backendpanol/scripts/qa/run-v2-smoke.mjs --env-file Producto/backendpanol/docs/qa/.env.qa
```

## 3) Resultados generados
La corrida crea una carpeta timestamp en:

`Producto/backendpanol/docs/qa/reports/<RUN_ID>/`

Archivos:
- `results.json`: detalle request por request (timestamp, endpoint, payload resumido, status, code).
- `summary.md`: cobertura y checks globales.
- `findings.md`: hallazgos reproducibles con severidad (`P0/P1/P2`).
- `remediation-plan.md`: plan de corrección sugerido por impacto.

## 4) Correlación con logs Cloud Run
Ejemplo de uso:

```powershell
./Producto/backendpanol/scripts/qa/cloudrun-log-extract.ps1 `
  -ProjectId "tu-project-id" `
  -ServiceName "backendpanol-dev" `
  -Region "us-central1" `
  -StartTimeIso "2026-05-11T12:00:00Z" `
  -EndTimeIso "2026-05-11T12:30:00Z" `
  -ContainsText "/api/v2/implements" `
  -OutputFile "Producto/backendpanol/docs/qa/reports/logs-implements.json"
```

## 5) Criterios de cierre investigación
- 31 endpoints v2 ejercitados en smoke (nominal + negativos críticos).
- Confirmación explícita de `sin 500` o detalle de persistencia con evidencia.
- Hallazgos clasificados y priorizados con plan de remediación listo.

## 6) Gate para corte UUID-only
Antes de ejecutar `uuid-final-cutover.sql`, exigir 2 corridas QA consecutivas verdes:

```powershell
node Producto/backendpanol/scripts/qa/validate-two-green-runs.mjs --reports-root Producto/backendpanol/docs/qa/reports --required-greens 2
```

Si el comando no retorna `Gate PASS`, se bloquea el corte DB.
