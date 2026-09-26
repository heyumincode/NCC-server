-- 危险操作：仅在已确认的非生产环境执行。执行前必须备份并取得人工确认。
BEGIN;

DROP TABLE ticket_verification_log;
DROP TABLE staff_session;
DROP TABLE ticket_reservation;
DROP TABLE news_article;
DROP TABLE match_event;
DROP TABLE user_session;
DROP TABLE app_user;

COMMIT;

