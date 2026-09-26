-- 仅限本地开发和预览环境，不作为生产 migration 执行。
INSERT INTO match_event (
    competition_name,
    round_name,
    home_team,
    away_team,
    venue,
    venue_address,
    kickoff_at,
    admission_at,
    booking_start_at,
    booking_end_at,
    capacity,
    reserved_count,
    status,
    cover_image_url,
    notice
)
SELECT
    '充超联赛',
    '揭幕战',
    '顺庆代表队',
    '高坪代表队',
    '南充市体育中心',
    '四川省南充市顺庆区',
    CURRENT_TIMESTAMP + INTERVAL '7 days',
    CURRENT_TIMESTAMP + INTERVAL '7 days' - INTERVAL '90 minutes',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP + INTERVAL '6 days',
    2000,
    0,
    'PUBLISHED',
    '',
    E'请提前 60 分钟到场。入场时出示电子票二维码，并配合现场安检。\n禁止携带危险品、玻璃容器及其他场馆禁限带物品。'
WHERE NOT EXISTS (
    SELECT 1 FROM match_event WHERE round_name = '揭幕战'
);

INSERT INTO news_article (title, summary, cover_image_url, content, status, published_at)
SELECT
    '充超联赛首场赛事预约即将开放',
    '九县市区球队齐聚绿茵，首场赛事免费预约观赛。',
    '',
    E'充超联赛首场赛事即将开赛。\n\n本场比赛采用免费预约、凭电子票入场的方式。请通过官方小程序完成预约，并关注赛前公告。',
    'PUBLISHED',
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM news_article WHERE title = '充超联赛首场赛事预约即将开放'
);

INSERT INTO news_article (title, summary, cover_image_url, content, status, published_at)
SELECT
    '首场比赛观赛须知',
    '入场时间、电子票使用方式和安检提醒。',
    '',
    E'请观众合理安排出行，提前到达场馆。\n\n电子票一人一票，仅限指定场次使用；已核销、已取消或伪造票不得入场。',
    'PUBLISHED',
    CURRENT_TIMESTAMP - INTERVAL '1 hour'
WHERE NOT EXISTS (
    SELECT 1 FROM news_article WHERE title = '首场比赛观赛须知'
);

