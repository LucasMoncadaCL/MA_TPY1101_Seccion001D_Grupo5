- Estado del documento: vigente
- Ultima verificacion: 2026-05-15
- Fuente de verdad: ver matriz canonica vigente y codigo fuente actual

# Flujo Completo de Migraciones (Flyway)

Este documento define el flujo estÃ¡ndar para crear, revisar, desplegar y validar migraciones SQL en el proyecto.

## Objetivo

- Evitar cambios manuales en producciÃ³n.
- Asegurar trazabilidad (quiÃ©n cambiÃ³ quÃ© y cuÃ¡ndo).
- Reducir riesgo de caÃ­da por cambios de esquema.
- Mantener compatibilidad entre cÃ³digo y base de datos en cada release.

## Principios

- Migraciones **forward-only** (`Vn__*.sql`), sin editar versiones ya aplicadas.
- Toda migraciÃ³n viaja en PR junto al cÃ³digo que la consume.
- Cada cambio de esquema debe ser compatible con el estado anterior (estrategia expand/contract cuando aplique).
- Si hay dual-key (ej. `id` + `uuid`), primero expandir, luego migrar uso, despuÃ©s contraer.

## Flujo Normal (end-to-end)

1. **DiseÃ±o en rama de feature**
- Crear nuevo script en `src/main/resources/db/migration/` con nombre `V{n}__descripcion.sql`.
- Incluir `IF EXISTS / IF NOT EXISTS` cuando sea necesario para robustez entre ambientes.
- Si hay backfill, dejarlo explÃ­cito en el script.

2. **Prueba local**
- Levantar backend contra BD local.
- Verificar que Flyway aplique la migraciÃ³n al iniciar.
- Probar:
  - BD limpia.
  - BD con datos existentes.
- Validar que el backend sigue operativo (login, endpoints crÃ­ticos).

3. **Pull Request**
- Incluir en PR:
  - SQL de migraciÃ³n.
  - cambios backend/frontend relacionados.
  - plan de rollback.
  - validaciones post-migraciÃ³n (queries/checks).
- Review tÃ©cnico de DB + backend.

4. **Merge a rama principal**
- Al mergear, la migraciÃ³n queda versionada y lista para despliegue.

5. **Deploy por ambiente (dev -> staging -> prod)**
- El backend arranca.
- Flyway detecta scripts pendientes en `flyway_schema_history`.
- Aplica en orden de versiÃ³n.
- Si falla una migraciÃ³n, el deploy se considera fallido y se detiene.

6. **ValidaciÃ³n post-deploy**
- Revisar logs de arranque Flyway.
- Ejecutar checks de integridad definidos (conteos, nulos, orphan rows, etc.).
- Validar flujos funcionales crÃ­ticos de negocio.

7. **Monitoreo**
- Seguir mÃ©tricas de errores API, latencia y locks DB.
- En migraciones por fases, medir adopciÃ³n (ej. uso de UUID vs ID legado).

## CuÃ¡ndo se ejecuta una migraciÃ³n

- Regla general: **se ejecuta al desplegar**, despuÃ©s del merge, al iniciar el backend del ambiente objetivo.
- No se recomienda ejecutar SQL manual directo en producciÃ³n fuera del pipeline, salvo contingencia aprobada.

## Estrategia recomendada de releases (Expand / Migrate / Contract)

1. **Expand**
- Agregar nuevas columnas/tablas/Ã­ndices sin romper lo existente.

2. **Migrate**
- Cambiar backend/frontend para escribir y leer el nuevo esquema.
- Mantener compatibilidad temporal (dual-write/dual-read si aplica).

3. **Contract**
- Cuando todo el trÃ¡fico usa el nuevo esquema, remover legacy (columnas, FKs, claims, endpoints).

## Checklist mÃ­nimo por migraciÃ³n

- Script versionado y con nombre correcto.
- No modifica migraciones antiguas ya ejecutadas.
- Ejecuta en local sin errores.
- Incluye validaciÃ³n de integridad.
- Incluye plan de rollback operativo.
- PR aprobado por al menos 1 revisor tÃ©cnico.

## Rollback: quÃ© sÃ­ y quÃ© no

- **SÃ­**: rollback de aplicaciÃ³n (volver a versiÃ³n anterior) si la migraciÃ³n fue expandible y backward-compatible.
- **No ideal**: rollback destructivo de esquema en caliente.
- Para cambios crÃ­ticos: usar backup/snapshot previo y plan de recuperaciÃ³n probado.

## Ejemplo aplicado al caso UUID

1. Deploy 1: migraciones de expansiÃ³n (`uuid`, `*_uuid`, Ã­ndices, FKs paralelas, triggers de sync).
2. Deploy 2: backend/frontend consumen UUID como principal; `id` queda legado temporal.
3. Deploy 3: corte final (retiro de legado) con ventana controlada y validaciones reforzadas.

