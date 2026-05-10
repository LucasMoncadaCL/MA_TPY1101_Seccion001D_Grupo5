ALTER TABLE public.implement ADD COLUMN IF NOT EXISTS category_uuid uuid;
ALTER TABLE public.implement ADD COLUMN IF NOT EXISTS location_uuid uuid;
ALTER TABLE public.individual ADD COLUMN IF NOT EXISTS implement_uuid uuid;
ALTER TABLE public.individual ADD COLUMN IF NOT EXISTS current_location_uuid uuid;
ALTER TABLE public.loan ADD COLUMN IF NOT EXISTS requester_uuid uuid;
ALTER TABLE public.loan ADD COLUMN IF NOT EXISTS room_uuid uuid;
ALTER TABLE public.loan_detail ADD COLUMN IF NOT EXISTS loan_uuid uuid;
ALTER TABLE public.loan_detail ADD COLUMN IF NOT EXISTS implement_uuid uuid;
ALTER TABLE public.loan_detail_individual ADD COLUMN IF NOT EXISTS loan_detail_uuid uuid;
ALTER TABLE public.loan_detail_individual ADD COLUMN IF NOT EXISTS individual_uuid uuid;
ALTER TABLE public.stock ADD COLUMN IF NOT EXISTS implement_uuid uuid;
ALTER TABLE public."user" ADD COLUMN IF NOT EXISTS role_uuid uuid;
ALTER TABLE public.audit_log ADD COLUMN IF NOT EXISTS actor_user_uuid uuid;
ALTER TABLE public.audit_log ADD COLUMN IF NOT EXISTS target_user_uuid uuid;
ALTER TABLE public.token_revocation ADD COLUMN IF NOT EXISTS user_uuid uuid;

UPDATE public.implement i
SET category_uuid = c.uuid
FROM public.category c
WHERE i.category_id = c.id
  AND i.category_uuid IS NULL;

UPDATE public.implement i
SET location_uuid = l.uuid
FROM public.location l
WHERE i.location_id = l.id
  AND i.location_uuid IS NULL;

UPDATE public.individual i
SET implement_uuid = imp.uuid
FROM public.implement imp
WHERE i.implement_id = imp.id
  AND i.implement_uuid IS NULL;

UPDATE public.individual i
SET current_location_uuid = l.uuid
FROM public.location l
WHERE i.current_location_id = l.id
  AND i.current_location_uuid IS NULL;

UPDATE public.loan l
SET requester_uuid = u.uuid
FROM public."user" u
WHERE l.requester_id = u.id
  AND l.requester_uuid IS NULL;

UPDATE public.loan l
SET room_uuid = r.uuid
FROM public.room r
WHERE l.room_id = r.id
  AND l.room_uuid IS NULL;

UPDATE public.loan_detail d
SET loan_uuid = l.uuid
FROM public.loan l
WHERE d.loan_id = l.id
  AND d.loan_uuid IS NULL;

UPDATE public.loan_detail d
SET implement_uuid = i.uuid
FROM public.implement i
WHERE d.implement_id = i.id
  AND d.implement_uuid IS NULL;

UPDATE public.loan_detail_individual di
SET loan_detail_uuid = d.uuid
FROM public.loan_detail d
WHERE di.loan_detail_id = d.id
  AND di.loan_detail_uuid IS NULL;

UPDATE public.loan_detail_individual di
SET individual_uuid = i.uuid
FROM public.individual i
WHERE di.individual_id = i.id
  AND di.individual_uuid IS NULL;

UPDATE public.stock s
SET implement_uuid = i.uuid
FROM public.implement i
WHERE s.implement_id = i.id
  AND s.implement_uuid IS NULL;

UPDATE public."user" u
SET role_uuid = r.uuid
FROM public.role r
WHERE u.role_id = r.id
  AND u.role_uuid IS NULL;

UPDATE public.audit_log a
SET actor_user_uuid = u.uuid
FROM public."user" u
WHERE a.actor_user_id = u.id
  AND a.actor_user_uuid IS NULL;

UPDATE public.audit_log a
SET target_user_uuid = u.uuid
FROM public."user" u
WHERE a.target_user_id = u.id
  AND a.target_user_uuid IS NULL;

UPDATE public.token_revocation t
SET user_uuid = u.uuid
FROM public."user" u
WHERE t.user_id = u.id
  AND t.user_uuid IS NULL;

CREATE INDEX IF NOT EXISTS idx_implement_category_uuid ON public.implement(category_uuid);
CREATE INDEX IF NOT EXISTS idx_implement_location_uuid ON public.implement(location_uuid);
CREATE INDEX IF NOT EXISTS idx_individual_implement_uuid ON public.individual(implement_uuid);
CREATE INDEX IF NOT EXISTS idx_individual_location_uuid ON public.individual(current_location_uuid);
CREATE INDEX IF NOT EXISTS idx_loan_requester_uuid ON public.loan(requester_uuid);
CREATE INDEX IF NOT EXISTS idx_loan_room_uuid ON public.loan(room_uuid);
CREATE INDEX IF NOT EXISTS idx_loan_detail_loan_uuid ON public.loan_detail(loan_uuid);
CREATE INDEX IF NOT EXISTS idx_loan_detail_implement_uuid ON public.loan_detail(implement_uuid);
CREATE INDEX IF NOT EXISTS idx_loan_detail_individual_detail_uuid ON public.loan_detail_individual(loan_detail_uuid);
CREATE INDEX IF NOT EXISTS idx_loan_detail_individual_individual_uuid ON public.loan_detail_individual(individual_uuid);
CREATE INDEX IF NOT EXISTS idx_stock_implement_uuid ON public.stock(implement_uuid);
CREATE INDEX IF NOT EXISTS idx_user_role_uuid ON public."user"(role_uuid);
CREATE INDEX IF NOT EXISTS idx_audit_actor_uuid ON public.audit_log(actor_user_uuid);
CREATE INDEX IF NOT EXISTS idx_audit_target_uuid ON public.audit_log(target_user_uuid);
CREATE INDEX IF NOT EXISTS idx_token_revocation_user_uuid ON public.token_revocation(user_uuid);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_implement_category_uuid') THEN
        ALTER TABLE public.implement ADD CONSTRAINT fk_implement_category_uuid FOREIGN KEY (category_uuid) REFERENCES public.category(uuid);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_implement_location_uuid') THEN
        ALTER TABLE public.implement ADD CONSTRAINT fk_implement_location_uuid FOREIGN KEY (location_uuid) REFERENCES public.location(uuid);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_individual_implement_uuid') THEN
        ALTER TABLE public.individual ADD CONSTRAINT fk_individual_implement_uuid FOREIGN KEY (implement_uuid) REFERENCES public.implement(uuid);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_individual_location_uuid') THEN
        ALTER TABLE public.individual ADD CONSTRAINT fk_individual_location_uuid FOREIGN KEY (current_location_uuid) REFERENCES public.location(uuid);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_loan_requester_uuid') THEN
        ALTER TABLE public.loan ADD CONSTRAINT fk_loan_requester_uuid FOREIGN KEY (requester_uuid) REFERENCES public."user"(uuid);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_loan_room_uuid') THEN
        ALTER TABLE public.loan ADD CONSTRAINT fk_loan_room_uuid FOREIGN KEY (room_uuid) REFERENCES public.room(uuid);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_loan_detail_loan_uuid') THEN
        ALTER TABLE public.loan_detail ADD CONSTRAINT fk_loan_detail_loan_uuid FOREIGN KEY (loan_uuid) REFERENCES public.loan(uuid);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_loan_detail_implement_uuid') THEN
        ALTER TABLE public.loan_detail ADD CONSTRAINT fk_loan_detail_implement_uuid FOREIGN KEY (implement_uuid) REFERENCES public.implement(uuid);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_loan_detail_individual_detail_uuid') THEN
        ALTER TABLE public.loan_detail_individual ADD CONSTRAINT fk_loan_detail_individual_detail_uuid FOREIGN KEY (loan_detail_uuid) REFERENCES public.loan_detail(uuid);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_loan_detail_individual_individual_uuid') THEN
        ALTER TABLE public.loan_detail_individual ADD CONSTRAINT fk_loan_detail_individual_individual_uuid FOREIGN KEY (individual_uuid) REFERENCES public.individual(uuid);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_stock_implement_uuid') THEN
        ALTER TABLE public.stock ADD CONSTRAINT fk_stock_implement_uuid FOREIGN KEY (implement_uuid) REFERENCES public.implement(uuid);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_user_role_uuid') THEN
        ALTER TABLE public."user" ADD CONSTRAINT fk_user_role_uuid FOREIGN KEY (role_uuid) REFERENCES public.role(uuid);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_audit_actor_user_uuid') THEN
        ALTER TABLE public.audit_log ADD CONSTRAINT fk_audit_actor_user_uuid FOREIGN KEY (actor_user_uuid) REFERENCES public."user"(uuid);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_audit_target_user_uuid') THEN
        ALTER TABLE public.audit_log ADD CONSTRAINT fk_audit_target_user_uuid FOREIGN KEY (target_user_uuid) REFERENCES public."user"(uuid);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_token_revocation_user_uuid') THEN
        ALTER TABLE public.token_revocation ADD CONSTRAINT fk_token_revocation_user_uuid FOREIGN KEY (user_uuid) REFERENCES public."user"(uuid);
    END IF;
END
$$;

CREATE OR REPLACE VIEW public.migration_check_user_uuid AS
SELECT
    COUNT(*) FILTER (WHERE uuid IS NULL) AS users_without_uuid,
    COUNT(*) FILTER (WHERE auth_uuid IS NULL OR auth_uuid = '') AS users_without_auth_uuid
FROM public."user";

CREATE OR REPLACE VIEW public.migration_check_relationship_uuid AS
SELECT
    (SELECT COUNT(*) FROM public.implement WHERE category_id IS NOT NULL AND category_uuid IS NULL) AS implement_without_category_uuid,
    (SELECT COUNT(*) FROM public.implement WHERE location_id IS NOT NULL AND location_uuid IS NULL) AS implement_without_location_uuid,
    (SELECT COUNT(*) FROM public.loan_detail WHERE loan_id IS NOT NULL AND loan_uuid IS NULL) AS loan_detail_without_loan_uuid,
    (SELECT COUNT(*) FROM public.loan_detail WHERE implement_id IS NOT NULL AND implement_uuid IS NULL) AS loan_detail_without_implement_uuid;
