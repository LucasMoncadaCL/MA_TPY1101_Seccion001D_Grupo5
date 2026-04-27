# Despliegue (Docker y Kubernetes)

Guia de despliegue del backend con entornos de base de datos separados por variable:

- `APP_DB_ENV=docker`
- `APP_DB_ENV=supabase`

## Opciones de despliegue

1. `backendpanol/docker-compose.yaml`
   - Backend + Postgres local.
2. `Producto/docker-compose.yaml`
   - Stack backend + frontend.
3. `backendpanol/deploy/k8s/*.yaml`
   - Despliegue backend en Kubernetes.

## Que compose usar segun el caso

### Caso A: quieres backend con Postgres local

Usa:

- `Producto/backendpanol/docker-compose.yaml`

Porque:

- Este compose si incluye servicio `postgres`.
- Sirve para desarrollo del backend con BD local.

Comando:

```bash
cd Producto/backendpanol
docker compose up --build
```

### Caso B: quieres levantar app completa (frontend + backend)

Usa:

- `Producto/docker-compose.yaml`

Porque:

- Este compose no incluye `postgres`.
- Levanta solo `backend` + `frontend`.
- Backend se conecta segun `APP_DB_ENV` y variables DB configuradas.

Comando:

```bash
cd Producto
docker compose up --build
```

### Caso C: despliegue en cluster

Usa:

- `Producto/backendpanol/deploy/k8s/*.yaml`

Porque:

- Son manifiestos de Kubernetes (no Docker Compose).

## Aclaracion sobre `APP_DB_ENV` y Docker Compose

- `APP_DB_ENV` decide que set de variables DB usa Spring (`DB_DOCKER_*` o `DB_SUPABASE_*`).
- `APP_DB_ENV` no crea ni elimina servicios de Docker Compose.
- En `backendpanol/docker-compose.yaml`, el servicio `postgres` existe siempre en ese archivo.
- En `Producto/docker-compose.yaml`, no existe servicio `postgres`; solo backend + frontend.

## Requisitos

- Docker + Docker Compose
- Kubernetes + kubectl (solo para opcion k8s)
- Archivos locales de entorno/secrets creados

## Archivos que intervienen

En `Producto/backendpanol`:

- `.env.local`
- `secrets/application-secrets.properties`
- `secrets/db_password.txt` (solo para postgres local en compose backend)

En `Producto`:

- `.env` (copiado desde `.env.example`) para `Producto/docker-compose.yaml`

## 1) Backend + Postgres local (`backendpanol/docker-compose.yaml`)

### Configuracion minima

En `backendpanol/.env.local`:

```env
APP_DB_ENV=docker
DB_DOCKER_HOST=localhost
DB_DOCKER_PORT=5432
DB_DOCKER_NAME=panol
DB_DOCKER_USER=panol_user
DB_DOCKER_SSL_MODE=disable
```

En `backendpanol/secrets/application-secrets.properties`:

```properties
DB_DOCKER_PASSWORD=replace_me
```

En `backendpanol/secrets/db_password.txt`:

```text
replace_me
```

### Levantar

Desde `Producto/backendpanol`:

```bash
docker compose up --build
```

Notas:

- El contenedor backend recibe `APP_DB_ENV=docker`.
- `DB_DOCKER_HOST` se fuerza a `postgres` dentro del compose para conectar por red interna.
- El contenedor `postgres` usa `db_password.txt` como Docker secret.
- Si no hay migraciones SQL reales (mas alla de baseline), la BD local queda sin tablas.

## 2) Stack completo (`Producto/docker-compose.yaml`)

Este compose soporta ambos modos segun `APP_DB_ENV` en `Producto/.env`.

### Modo Supabase (recomendado para stack completo)

En `Producto/.env`:

```env
APP_DB_ENV=supabase
DB_SUPABASE_HOST=aws-1-us-east-1.pooler.supabase.com
DB_SUPABASE_PORT=5432
DB_SUPABASE_NAME=postgres
DB_SUPABASE_USER=postgres.xxx
DB_SUPABASE_PASSWORD=replace_me
DB_SUPABASE_SSL_MODE=require
```

En `backendpanol/secrets/application-secrets.properties` puedes repetir:

```properties
DB_SUPABASE_PASSWORD=replace_me
```

### Modo Docker DB

Si quieres forzar el backend del stack completo a DB docker:

```env
APP_DB_ENV=docker
DB_DOCKER_HOST=postgres
DB_DOCKER_PORT=5432
DB_DOCKER_NAME=panol
DB_DOCKER_USER=panol_user
DB_DOCKER_PASSWORD=replace_me
DB_DOCKER_SSL_MODE=disable
```

### Levantar

Desde `Producto`:

```bash
docker compose up --build
```

## 3) Kubernetes (`backendpanol/deploy/k8s`)

Manifiestos disponibles:

- `configmap.yaml`
- `secret.example.yaml`
- `deployment.yaml`
- `service.yaml`

### Configurar modo de BD

En `configmap.yaml`:

- `APP_DB_ENV` define el modo (`docker` o `supabase`).
- Completa `DB_SUPABASE_*` o `DB_DOCKER_*` segun corresponda.

En `secret.example.yaml`:

- Reemplaza `DB_SUPABASE_PASSWORD` por un valor real y guarda como secret real.

### Aplicar

Desde `Producto/backendpanol`:

```bash
kubectl apply -f deploy/k8s/configmap.yaml
kubectl apply -f deploy/k8s/secret.example.yaml
kubectl apply -f deploy/k8s/deployment.yaml
kubectl apply -f deploy/k8s/service.yaml
```

## Build de imagen backend (manual)

Desde `Producto/backendpanol`:

```bash
docker build \
  --build-arg JOOQ_DB_URL=jdbc:postgresql://host:5432/postgres?sslmode=require \
  --build-arg JOOQ_DB_USER=usuario \
  --build-arg JOOQ_DB_PASSWORD=secret \
  -t panol/backendpanol:latest .
```

Importante:

- El `Dockerfile` ejecuta `mvn package`.
- jOOQ codegen corre en build, por eso `JOOQ_DB_*` debe existir.

## Troubleshooting rapido

- `No JDBC Connection configured` en build:
  - Faltan `JOOQ_DB_*`.
- Backend no conecta a BD:
  - Revisar `APP_DB_ENV` y que exista el set correcto (`DB_DOCKER_*` o `DB_SUPABASE_*`).
- Error de password:
  - Revisar `secrets/application-secrets.properties` y/o variables del compose.
