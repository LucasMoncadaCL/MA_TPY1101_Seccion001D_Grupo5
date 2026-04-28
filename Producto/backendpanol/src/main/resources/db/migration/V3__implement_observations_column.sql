-- Adds optional observations for implement creation payload (HU-13 / PSD-105+)
ALTER TABLE public.implement
    ADD COLUMN IF NOT EXISTS observations VARCHAR(500);
