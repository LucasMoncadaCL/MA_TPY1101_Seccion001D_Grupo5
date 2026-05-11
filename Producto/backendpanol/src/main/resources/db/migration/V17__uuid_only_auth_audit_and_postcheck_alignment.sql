-- UUID-only extension moved out of V11/V13 to keep historical checksums stable.
-- Safe/idempotent additions for auth/audit tables after initial uuid migration.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE public.audit_log
    ADD COLUMN IF NOT EXISTS uuid uuid DEFAULT gen_random_uuid();

ALTER TABLE public.token_revocation
    ADD COLUMN IF NOT EXISTS uuid uuid DEFAULT gen_random_uuid();

UPDATE public.audit_log
SET uuid = gen_random_uuid()
WHERE uuid IS NULL;

UPDATE public.token_revocation
SET uuid = gen_random_uuid()
WHERE uuid IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_audit_log_uuid ON public.audit_log(uuid);
CREATE UNIQUE INDEX IF NOT EXISTS ux_token_revocation_uuid ON public.token_revocation(uuid);

ALTER TABLE public.audit_log
    ALTER COLUMN uuid SET NOT NULL;

ALTER TABLE public.token_revocation
    ALTER COLUMN uuid SET NOT NULL;

-- Align post-check views with auth/audit uuid columns.
CREATE OR REPLACE VIEW public.migration_check_uuid_auth_audit AS
SELECT 'audit_log.uuid.null'::text AS metric, COUNT(*)::bigint AS pending
FROM public.audit_log
WHERE uuid IS NULL
UNION ALL
SELECT 'token_revocation.uuid.null'::text AS metric, COUNT(*)::bigint AS pending
FROM public.token_revocation
WHERE uuid IS NULL
UNION ALL
SELECT 'audit_log.actor_user_uuid.null'::text AS metric, COUNT(*)::bigint AS pending
FROM public.audit_log
WHERE actor_user_uuid IS NULL
UNION ALL
SELECT 'audit_log.target_user_uuid.null'::text AS metric, COUNT(*)::bigint AS pending
FROM public.audit_log
WHERE target_user_uuid IS NULL
UNION ALL
SELECT 'token_revocation.user_uuid.null'::text AS metric, COUNT(*)::bigint AS pending
FROM public.token_revocation
WHERE user_uuid IS NULL;

CREATE OR REPLACE VIEW public.migration_check_uuid_only_columns AS
SELECT table_name, column_name
FROM information_schema.columns
WHERE table_schema = 'public'
  AND (
      column_name = 'id'
      OR column_name LIKE '%\_id' ESCAPE '\'
  )
  AND table_name <> 'flyway_schema_history';

CREATE OR REPLACE VIEW public.migration_check_uuid_only_nulls AS
SELECT 'user.uuid.null'::text AS metric, COUNT(*)::bigint AS pending FROM public."user" WHERE uuid IS NULL
UNION ALL
SELECT 'role.uuid.null'::text AS metric, COUNT(*)::bigint AS pending FROM public.role WHERE uuid IS NULL
UNION ALL
SELECT 'category.uuid.null'::text AS metric, COUNT(*)::bigint AS pending FROM public.category WHERE uuid IS NULL
UNION ALL
SELECT 'location.uuid.null'::text AS metric, COUNT(*)::bigint AS pending FROM public.location WHERE uuid IS NULL
UNION ALL
SELECT 'implement.uuid.null'::text AS metric, COUNT(*)::bigint AS pending FROM public.implement WHERE uuid IS NULL
UNION ALL
SELECT 'individual.uuid.null'::text AS metric, COUNT(*)::bigint AS pending FROM public.individual WHERE uuid IS NULL
UNION ALL
SELECT 'audit_log.uuid.null'::text AS metric, COUNT(*)::bigint AS pending FROM public.audit_log WHERE uuid IS NULL
UNION ALL
SELECT 'token_revocation.uuid.null'::text AS metric, COUNT(*)::bigint AS pending FROM public.token_revocation WHERE uuid IS NULL;
