select id, rut, email, active, failed_login_attempts, blocked_until, last_login_at
from public."user"
where rut='11222333' or email='director.carrera@duocuc.cl';

select id, event_type, metadata, created_at
from public.audit_log
where event_type in ('login_failed','user_logged_in')
order by id desc
limit 10;
