select id, event, payload, created_at
from public.audit_log
where event in ('login_failed','user_logged_in')
order by id desc
limit 20;
