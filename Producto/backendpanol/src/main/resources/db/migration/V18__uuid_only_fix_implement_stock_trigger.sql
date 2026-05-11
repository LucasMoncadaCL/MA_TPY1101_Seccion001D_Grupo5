-- UUID-only hardening for implement->stock synchronization.
-- Fixes legacy DB states where an old trigger still writes stock.implement_id.

DO $$
DECLARE
    rec RECORD;
BEGIN
    -- Drop any non-internal trigger on public.implement whose function body still references implement_id/stock.
    FOR rec IN
        SELECT t.tgname AS trigger_name, n.nspname AS schema_name, c.relname AS table_name
        FROM pg_trigger t
        JOIN pg_class c ON c.oid = t.tgrelid
        JOIN pg_namespace n ON n.oid = c.relnamespace
        JOIN pg_proc p ON p.oid = t.tgfoid
        WHERE NOT t.tgisinternal
          AND p.prokind = 'f'
          AND n.nspname = 'public'
          AND c.relname = 'implement'
          AND pg_get_functiondef(p.oid) ILIKE '%implement_id%'
          AND pg_get_functiondef(p.oid) ILIKE '%stock%'
    LOOP
        EXECUTE format('DROP TRIGGER IF EXISTS %I ON %I.%I', rec.trigger_name, rec.schema_name, rec.table_name);
    END LOOP;
END $$;

DO $$
DECLARE
    rec RECORD;
BEGIN
    -- Drop orphan legacy functions with implement_id/stock logic once no trigger references them.
    FOR rec IN
        SELECT p.oid,
               n.nspname AS schema_name,
               p.proname AS function_name,
               pg_get_function_identity_arguments(p.oid) AS args
        FROM pg_proc p
        JOIN pg_namespace n ON n.oid = p.pronamespace
        WHERE n.nspname = 'public'
          AND p.prokind = 'f'
          AND pg_get_functiondef(p.oid) ILIKE '%implement_id%'
          AND pg_get_functiondef(p.oid) ILIKE '%stock%'
          AND NOT EXISTS (
              SELECT 1
              FROM pg_trigger t
              WHERE NOT t.tgisinternal
                AND t.tgfoid = p.oid
          )
    LOOP
        EXECUTE format('DROP FUNCTION IF EXISTS %I.%I(%s)', rec.schema_name, rec.function_name, rec.args);
    END LOOP;
END $$;

CREATE OR REPLACE FUNCTION public.fn_implement_ensure_stock_uuid()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.uuid IS NOT NULL THEN
        INSERT INTO public.stock (implement_uuid)
        VALUES (NEW.uuid)
        ON CONFLICT (implement_uuid) DO NOTHING;
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_implement_ensure_stock_uuid ON public.implement;
CREATE TRIGGER trg_implement_ensure_stock_uuid
AFTER INSERT ON public.implement
FOR EACH ROW
EXECUTE FUNCTION public.fn_implement_ensure_stock_uuid();
