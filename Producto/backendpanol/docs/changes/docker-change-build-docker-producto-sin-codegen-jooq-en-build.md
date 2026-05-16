- Estado del documento: historico
- Ultima verificacion: 2026-05-15
- Fuente de verdad: ver matriz canonica vigente y codigo fuente actual

# Docker-change: Build Docker (Producto) sin depender de jOOQ codegen en build

Fecha: 2026-04-29

## Resumen

Se ajustÃ³ el build Docker del backend para que **no falle** cuando el jOOQ codegen (introspecciÃ³n DB) no puede ejecutarse durante el `docker compose build` (por lÃ­mites de pool / conectividad).

La estrategia es:

1) El codegen de jOOQ se genera **fuera** del build (local/CI) y queda disponible como artefacto en `target/generated-sources/jooq`.
2) El `Dockerfile` copia esas fuentes generadas y ejecuta el build con `-Djooq.codegen.skip=true`.

## Problema que resuelve

El `Dockerfile` del backend ejecuta `mvn package`, y el `pom.xml` corre jOOQ codegen en `generate-sources`.

En builds locales con Supabase pooler, jOOQ puede fallar por lÃ­mites de sesiones (`pool_size`) o por conectividad durante el build (BuildKit).

## Cambios realizados

### 1) `backendpanol/.dockerignore`

Antes se ignoraba todo `target/`, lo que impedÃ­a incluir el codegen.

Ahora:

- se sigue ignorando `target/*`
- pero se permite `target/generated-sources/jooq/**`

Archivo:

- `Producto/backendpanol/.dockerignore`

### 2) `backendpanol/Dockerfile`

Cambios:

- se copia `target/generated-sources/jooq` al stage de build
- se ejecuta Maven con `-Djooq.codegen.skip=true` para evitar correr el codegen dentro del build

Archivo:

- `Producto/backendpanol/Dockerfile`

## Requisito

Para que el build del backend funcione, debe existir:

- `Producto/backendpanol/target/generated-sources/jooq`

Si no existe, primero genera jOOQ fuera del build (segÃºn el flujo normal del proyecto) y luego reconstruye la imagen.

## CÃ³mo construir y levantar (stack â€œProductoâ€)

Desde `Producto/`:

```powershell
docker compose build backend
docker compose build frontend
docker compose up -d
```

## Notas

- Este cambio hace el build **mÃ¡s determinÃ­stico**, ya que deja de depender de conectividad DB durante la construcciÃ³n de la imagen.
- jOOQ codegen sigue existiendo, pero se mueve a un paso previo (fuera del Docker build).



## Advertencia historica

Este documento conserva contexto tecnico de una etapa anterior. No debe usarse como guia operativa primaria sin contrastar con la documentacion vigente.

## Estado actual (vigente)

- Contratos publicos: solo /api/v2/**.
- Seguridad: permitAll solo en POST /api/v2/auth/login (+ health/info).
- Eventos: outbox operativo con estados PENDING/PROCESSED/FAILED.
- Compose principal: Producto/docker-compose.yaml (frontend + backend, sin postgres local).

