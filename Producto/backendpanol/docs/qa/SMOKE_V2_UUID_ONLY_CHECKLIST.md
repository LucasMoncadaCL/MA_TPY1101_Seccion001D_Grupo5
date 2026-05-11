# Smoke Checklist v2 UUID-Only

## Objetivo
Validar rápidamente que el backend `v2` está estable, sin errores `500` y sin regresiones legacy (`id` numérico) en flujos críticos.

## Precondiciones
- Backend desplegado en entorno objetivo.
- Usuario con token válido por rol (`DIRECTOR`, `COORDINADOR`, `DOCENTE` según endpoint).
- Al menos un implemento activo y una categoría/ubicación activa.

## Matriz de Smoke (HTTP)
1. `POST /api/v2/auth/login`
2. `POST /api/v2/auth/logout`
3. `GET /api/v2/categories/active`
4. `GET /api/v2/categories/gestion`
5. `POST /api/v2/categories`
6. `PUT /api/v2/categories/{categoryUuid}`
7. `PATCH /api/v2/categories/{categoryUuid}/deactivate`
8. `DELETE /api/v2/categories/{categoryUuid}`
9. `GET /api/v2/locations`
10. `GET /api/v2/locations/management`
11. `POST /api/v2/locations`
12. `PUT /api/v2/locations/{locationUuid}`
13. `PATCH /api/v2/locations/{locationUuid}/active`
14. `GET /api/v2/implements`
15. `GET /api/v2/implements/{implementUuid}`
16. `POST /api/v2/implements`
17. `PUT /api/v2/implements/{implementUuid}`
18. `PATCH /api/v2/implements/{implementUuid}/active`
19. `GET /api/v2/implements/movements`
20. `POST /api/v2/implements/{implementUuid}/movements`
21. `GET /api/v2/implements/{implementUuid}/stock`
22. `POST /api/v2/implements/{implementUuid}/stock/entries`
23. `POST /api/v2/implements/{implementUuid}/stock/movements`
24. `PUT /api/v2/implements/{implementUuid}/stock/individuals/{individualUuid}`
25. `GET /api/v2/implements/{implementUuid}/labels/pdf`
26. `GET /api/v2/users`
27. `POST /api/v2/users`
28. `PUT /api/v2/users/{userUuid}`
29. `PUT /api/v2/users/{userUuid}/role`
30. `PATCH /api/v2/users/{userUuid}/active`
31. `DELETE /api/v2/users/{userUuid}`

## Validaciones esperadas por endpoint
- Caso nominal: `2xx` (sin `500`).
- Input inválido: `400`.
- Recurso inexistente: `404`.
- Sin permisos: `403`/`401` según corresponda.
- Recurso inactivo / estado no permitido: `400`/`409` controlado.

## Checks globales obligatorios
1. `GET /api/v1/...` debe responder bloqueado (`401/403/404` según capa).
2. Cero `NullPointerException` en logs de Cloud Run durante smoke.
3. Cero `BadSqlGrammarException` / ambigüedad SQL en logs.
4. Cero fallos de startup (`Flyway`, `prepared statement`, `pooler`) en revisión nueva.

## Salida mínima para cierre de release
- Lista de endpoints probados con `status code` y timestamp.
- Enlace a logs de Cloud Run del intervalo de prueba.
- Confirmación explícita: `sin 500`, `sin legacy id funcional`.
