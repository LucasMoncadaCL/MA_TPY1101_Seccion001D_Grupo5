select id, name, rut, email, active, role_id, failed_login_attempts, blocked_until
from public."user"
where replace(replace(replace(rut,'.',''),'-',''),' ','')='11222333';
