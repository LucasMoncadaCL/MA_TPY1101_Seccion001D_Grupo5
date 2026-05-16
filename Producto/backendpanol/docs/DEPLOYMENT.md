# Despliegue (Docker y Kubernetes)

- Estado del documento: vigente
- Ultima verificacion: 2026-05-15
- Fuente de verdad: `Producto/docker-compose.yaml`, `Producto/backendpanol/docker-compose.yaml`, `deploy/k8s/*.yaml`

## Opciones

1. `Producto/docker-compose.yaml`
- Stack `frontend + backend`.
- No incluye postgres local.

2. `Producto/backendpanol/docker-compose.yaml`
- Backend only.

3. `Producto/backendpanol/deploy/k8s/*.yaml`
- Despliegue en Kubernetes.

## Comandos

Stack completo:
```bash
cd Producto
docker compose up --build
```

Backend only:
```bash
cd Producto/backendpanol
docker compose up --build
```

## Configuracion de BD

- `APP_DB_ENV=supabase`: backend usa `DB_SUPABASE_*`.
- `APP_DB_ENV=docker`: backend usa `DB_DOCKER_*`.

## Seguridad en despliegue

- `APP_SECURITY_ENABLED=true` por defecto.
- `POST /api/v2/auth/login` es el unico endpoint publico de auth.

## Nota operativa

No documentar ni usar rutas legacy en despliegues actuales (`/api/categorias`, `/api/implements`, `/api/v1/**`).

