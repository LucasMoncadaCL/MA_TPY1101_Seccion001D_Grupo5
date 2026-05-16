UPDATE public."user"
SET email = 'pending+' || uuid::text || '@panol.local'
WHERE email IS NULL OR btrim(email) = '';

ALTER TABLE public."user"
    ALTER COLUMN email SET NOT NULL;
