CREATE OR REPLACE FUNCTION public.fn_sync_user_role_keys()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.role_uuid IS NULL AND NEW.role_id IS NOT NULL THEN
        SELECT r.uuid INTO NEW.role_uuid FROM public.role r WHERE r.id = NEW.role_id;
    END IF;

    IF NEW.role_id IS NULL AND NEW.role_uuid IS NOT NULL THEN
        SELECT r.id INTO NEW.role_id FROM public.role r WHERE r.uuid = NEW.role_uuid;
    END IF;

    IF NEW.auth_uuid IS NULL OR NEW.auth_uuid = '' THEN
        NEW.auth_uuid := NEW.uuid::text;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_sync_user_role_keys ON public."user";
CREATE TRIGGER trg_sync_user_role_keys
BEFORE INSERT OR UPDATE ON public."user"
FOR EACH ROW
EXECUTE FUNCTION public.fn_sync_user_role_keys();

CREATE OR REPLACE FUNCTION public.fn_validate_uuid_migration()
RETURNS TABLE(metric text, pending bigint)
LANGUAGE sql
AS $$
    SELECT 'user.uuid.null', COUNT(*)::bigint FROM public."user" WHERE uuid IS NULL
    UNION ALL
    SELECT 'user.role_uuid.null', COUNT(*)::bigint FROM public."user" WHERE role_id IS NOT NULL AND role_uuid IS NULL
    UNION ALL
    SELECT 'loan.requester_uuid.null', COUNT(*)::bigint FROM public.loan WHERE requester_id IS NOT NULL AND requester_uuid IS NULL
    UNION ALL
    SELECT 'loan_detail.loan_uuid.null', COUNT(*)::bigint FROM public.loan_detail WHERE loan_id IS NOT NULL AND loan_uuid IS NULL
    UNION ALL
    SELECT 'stock.implement_uuid.null', COUNT(*)::bigint FROM public.stock WHERE implement_id IS NOT NULL AND implement_uuid IS NULL;
$$;
