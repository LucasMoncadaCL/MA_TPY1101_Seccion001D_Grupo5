# PSD-111 - Build Docker (Producto) sin depender de jOOQ codegen en build

Fecha: 2026-04-29

## Resumen

Se ajustó el build Docker del backend para que **no falle** cuando el jOOQ codegen (introspección DB) no puede ejecutarse durante el `docker compose build` (por límites de pool / conectividad).

La estrategia es:

1) El codegen de jOOQ se genera **fuera** del build (local/CI) y queda versionado como artefacto en `target/generated-sources/jooq`.
2) El `Dockerfile` copia esas fuentes generadas y ejecuta el build con `-Djooq.codegen.skip=true`.

## Problema que resuelve

El `Dockerfile` del backend ejecuta `mvn package`, y el `pom.xml` corre jOOQ codegen en `generate-sources`.

En builds locales con Supabase pooler, jOOQ puede fallar por límites de sesiones (`pool_size`) o por conectividad durante el build (BuildKit).

## Cambios realizados

### 1) `backendpanol/.dockerignore`

Antes se ignoraba todo `target/`, lo que impedía incluir el codegen.

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

Si no existe, primero genera jOOQ fuera del build (según el flujo normal del proyecto) y luego reconstruye la imagen.

## Cómo construir y levantar (stack “Producto”)

Desde `Producto/`:

```powershell
docker compose --env-file .\\backendpanol\\.env.local build backend
docker compose --env-file .\\backendpanol\\.env.local build frontend
docker compose --env-file .\\backendpanol\\.env.local up -d
```

## Notas

- Este cambio hace el build **más determinístico**, ya que deja de depender de conectividad DB durante la construcción de la imagen.
- jOOQ codegen sigue existiendo, pero se mueve a un paso previo (fuera del Docker build).

