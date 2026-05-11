-- Ensure stock.implement_uuid can be used in ON CONFLICT safely.
-- Fixes runtime 500 on POST /api/v2/implements when trigger fn_implement_ensure_stock_uuid executes.

-- 1) Remove duplicate non-null implement_uuid rows, keeping one row per implement.
WITH ranked AS (
    SELECT
        ctid,
        ROW_NUMBER() OVER (PARTITION BY implement_uuid ORDER BY ctid) AS rn
    FROM public.stock
    WHERE implement_uuid IS NOT NULL
)
DELETE FROM public.stock s
USING ranked r
WHERE s.ctid = r.ctid
  AND r.rn > 1;

-- 2) Guarantee uniqueness so ON CONFLICT (implement_uuid) can be inferred.
CREATE UNIQUE INDEX IF NOT EXISTS ux_stock_implement_uuid_unique
    ON public.stock (implement_uuid);
