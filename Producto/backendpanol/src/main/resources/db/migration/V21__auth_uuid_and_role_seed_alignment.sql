ALTER TABLE public."user"
    ADD COLUMN IF NOT EXISTS auth_uuid varchar(255);

UPDATE public."user"
SET auth_uuid = uuid::text
WHERE (auth_uuid IS NULL OR auth_uuid = '')
  AND uuid IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_user_auth_uuid
    ON public."user"(auth_uuid);

DO
$$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'role'
          AND column_name = 'uuid'
    ) THEN
        IF NOT EXISTS (SELECT 1 FROM public.role WHERE UPPER(name) = 'DIRECTOR') THEN
            INSERT INTO public.role (uuid, name) VALUES (gen_random_uuid(), 'DIRECTOR');
        END IF;
        IF NOT EXISTS (SELECT 1 FROM public.role WHERE UPPER(name) = 'COORDINADOR') THEN
            INSERT INTO public.role (uuid, name) VALUES (gen_random_uuid(), 'COORDINADOR');
        END IF;
        IF NOT EXISTS (SELECT 1 FROM public.role WHERE UPPER(name) = 'DOCENTE') THEN
            INSERT INTO public.role (uuid, name) VALUES (gen_random_uuid(), 'DOCENTE');
        END IF;
    ELSE
        IF NOT EXISTS (SELECT 1 FROM public.role WHERE UPPER(name) = 'DIRECTOR') THEN
            INSERT INTO public.role (name) VALUES ('DIRECTOR');
        END IF;
        IF NOT EXISTS (SELECT 1 FROM public.role WHERE UPPER(name) = 'COORDINADOR') THEN
            INSERT INTO public.role (name) VALUES ('COORDINADOR');
        END IF;
        IF NOT EXISTS (SELECT 1 FROM public.role WHERE UPPER(name) = 'DOCENTE') THEN
            INSERT INTO public.role (name) VALUES ('DOCENTE');
        END IF;
    END IF;
END
$$;
