-- Final UUID-only enforcement checks.
-- This migration is intentionally strict: it fails if legacy numeric business identifiers remain.

DO $$
DECLARE
    rec RECORD;
BEGIN
    FOR rec IN
        SELECT table_name, column_name
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND (
            column_name = 'id'
            OR column_name LIKE '%\_id' ESCAPE '\'
          )
          AND table_name NOT IN ('flyway_schema_history')
          AND column_name NOT IN ('_id') -- Mongo internal document id is out of SQL scope
    LOOP
        RAISE EXCEPTION 'UUID-ONLY ENFORCEMENT FAILED: legacy column %.% still exists', rec.table_name, rec.column_name;
    END LOOP;
END $$;

DO $$
DECLARE
    rec RECORD;
BEGIN
    FOR rec IN
        SELECT tc.table_name, tc.constraint_name
        FROM information_schema.table_constraints tc
        JOIN information_schema.key_column_usage kcu
          ON tc.constraint_name = kcu.constraint_name
         AND tc.table_schema = kcu.table_schema
        WHERE tc.table_schema = 'public'
          AND tc.constraint_type = 'PRIMARY KEY'
          AND kcu.column_name = 'id'
    LOOP
        RAISE EXCEPTION 'UUID-ONLY ENFORCEMENT FAILED: numeric PK constraint % on table % still exists', rec.constraint_name, rec.table_name;
    END LOOP;
END $$;

CREATE OR REPLACE VIEW public.migration_check_uuid_only_enforced AS
SELECT 'legacy_id_columns'::text AS metric, COUNT(*)::bigint AS pending
FROM information_schema.columns
WHERE table_schema = 'public'
  AND (column_name = 'id' OR column_name LIKE '%\_id' ESCAPE '\')
  AND table_name NOT IN ('flyway_schema_history')
  AND column_name <> '_id';
