-- 1
SELECT login, registration_date
	FROM library.users
	WHERE registration_date > '2023-01-01';

-- 2
SELECT id, name, price
  FROM library.games
  WHERE price BETWEEN 10::money AND 30::money;

 -- 3
SELECT name, description
  FROM library.genres
  WHERE name LIKE '%RPG%'

-- 4
SELECT publisher_name, game_id
  FROM library.publishers_games_genres
  WHERE publisher_name IN (
    SELECT name
      FROM library.publishers
      WHERE country LIKE '%Russia%'
  );

-- 5
SELECT name, country
  FROM library.publishers AS p
  WHERE NOT EXISTS (
      SELECT publisher_name
      FROM library.publishers_games_genres pgg
      WHERE publisher_name = p.name
  );

-- 6
SELECT name, price
  FROM library.games
  WHERE price < ALL (
    SELECT g.price
      FROM library.games g
      JOIN library.publishers_games_genres gg ON g.id = gg.game_id
      WHERE gg.genre_name LIKE '%RPG%'
  );


-- 7
SELECT 
    COUNT(*) as total_purchases,
    SUM(g.price::numeric)::money as total_revenue,
    AVG(g.price::numeric)::money as avg_price,
    MIN(g.price) as min_price,
    MAX(g.price) as max_price
FROM library.users_games ug
JOIN library.games g ON ug.game_id = g.id;

-- 8
SELECT 
    g.id,
    g.name,
    g.price,
    (SELECT AVG(price::numeric) FROM library.games)::money as avg_all_games,
    (SELECT COUNT(*) FROM library.users_games WHERE game_id = g.id) as purchase_count
FROM library.games g;

-- 9
SELECT 
  game_id,
  purchase_date,
  CASE purchase_date
    WHEN CURRENT_DATE THEN 'Today'
    WHEN CURRENT_DATE - 1 THEN 'Yesterday'
    ELSE CAST(CURRENT_DATE - purchase_date AS VARCHAR) || ' days ago'
  END as purchase_category
FROM library.users_games;

-- 10
SELECT 
  name,
  price,
  case
    WHEN price::numeric = 0 THEN 'Free'
    WHEN price::numeric < 20 THEN 'Cheap'
    WHEN price::numeric >= 10 THEN 'Expensive'
  end as price_category
FROM library.games;

-- 11
SELECT 
    g.id,
    g.name,
    COUNT(ug.game_id) AS purchase_count,
    SUM(g.price::numeric)::money AS total_revenue
INTO TEMP TABLE popular_games
FROM library.games AS g
JOIN library.users_games ug ON g.id = ug.game_id
GROUP BY g.id, g.name
HAVING COUNT(ug.game_id) >= 5;

SELECT *
  FROM popular_games;

-- 12 
SELECT 
    'By purchase count' as criteria,
    pg.name as best_game,
    pg.purchase_count as value
FROM popular_games pg
WHERE pg.purchase_count = (SELECT MAX(purchase_count) FROM popular_games)

UNION

SELECT 
    'By revenue' as criteria,
    pg.name as best_game,
    pg.total_revenue::numeric as value
FROM popular_games pg
WHERE pg.total_revenue = (SELECT MAX(total_revenue) FROM popular_games);

-- 13
SELECT 
    u.login,
    u.nickname,
    (SELECT COUNT(*) 
     FROM library.users_games 
     WHERE user_login = u.login) as game_count
FROM library.users u
WHERE (SELECT COUNT(*) 
       FROM library.users_games 
       WHERE user_login = u.login) = (
    SELECT MAX(game_count)
    FROM (
        SELECT COUNT(*) as game_count
        FROM library.users_games
        GROUP BY user_login
    ) as user_stats
);

-- 14
SELECT 
    gen.name as genre_name,
    COUNT(pgg.game_id) as games_count,
    BOOL_AND(gen.singleplayer) as has_singleplayer
FROM library.genres gen
LEFT JOIN library.publishers_games_genres pgg ON gen.name = pgg.genre_name
GROUP BY gen.name
ORDER BY games_count DESC;

-- 15
SELECT 
    p.country,
    AVG(p.rating) as avg_rating,
    COUNT(*) as publishers_count
FROM library.publishers p
GROUP BY p.country
HAVING AVG(p.rating) > (SELECT AVG(rating) FROM library.publishers)
ORDER BY avg_rating DESC;

-- 16
INSERT INTO library.games (id, name, description, price)
  values (DEFAULT, 'Master-Slave', 'Game about connectors', 1337);

-- 17
INSERT INTO library.users_games (user_login, game_id, purchase_date)
  SELECT 
    'aaronbyrd' as user_login,
    id as game_id,
    current_date as purchase_date
  FROM library.games
  WHERE price::numeric < 20;

-- 18
UPDATE library.games
SET price = price * 1.1::numeric
WHERE id IN (
    SELECT game_id
    FROM library.publishers_games_genres 
    WHERE genre_name = 'Action'
);

-- 19
UPDATE library.publishers
SET rating = (
    SELECT AVG(rating)
    FROM library.publishers 
    WHERE country LIKE '%Russia%'
)
WHERE name = 'Meta LLC';

-- 20
DELETE FROM library.users
WHERE registration_date < '2020-01-01';

-- 21
DELETE FROM library.games
WHERE id NOT IN (
    SELECT DISTINCT game_id 
    FROM library.users_games
) AND id NOT IN (
    SELECT DISTINCT game_id 
    FROM library.publishers_games_genres
);

-- 22
WITH UserPurchaseStats AS (
    SELECT 
        u.login,
        u.nickname,
        COUNT(ug.game_id) as total_games,
        SUM(g.price::numeric)::money as total_spent
    FROM library.users u
    LEFT JOIN library.users_games ug ON u.login = ug.user_login
    LEFT JOIN library.games g ON ug.game_id = g.id
    GROUP BY u.login, u.nickname
)
SELECT 
    login,
    nickname,
    total_games,
    total_spent,
    total_spent::numeric / NULLIF(total_games, 0) as avg_game_price
FROM UserPurchaseStats
ORDER BY total_spent DESC;

-- 23
WITH RECURSIVE date_sequence AS (
    SELECT date('2020-03-01') as generated_date, 1 as day_number
    
    UNION ALL
    
    SELECT generated_date - 1, day_number + 1
    FROM date_sequence
    WHERE day_number < 7
)
SELECT 
    ds.generated_date,
    COUNT(ug.purchase_date) as purchases_count,
    COALESCE(SUM(g.price::numeric), 0)::money as daily_revenue
FROM date_sequence ds
LEFT JOIN library.users_games ug ON ug.purchase_date = ds.generated_date
LEFT JOIN library.games g ON ug.game_id = g.id
GROUP BY ds.generated_date
ORDER BY ds.generated_date DESC;

-- 24
SELECT 
    g.id,
    g.name,
    g.price,
    gen.name as genre_name,
    RANK() OVER (PARTITION BY pgg.genre_name ORDER BY g.price DESC) as price_rank_in_genre,
    AVG(g.price::numeric) OVER (PARTITION BY pgg.genre_name)::money as avg_genre_price
FROM library.games g
JOIN library.publishers_games_genres pgg ON g.id = pgg.game_id
JOIN library.genres gen ON pgg.genre_name = gen.name;

-- 25
WITH UserDuplicates AS (
    SELECT 
        login,
        password,
        nickname,
        registration_date,
        ROW_NUMBER() OVER (PARTITION BY login ORDER BY registration_date) as rn
    FROM library.users
)
SELECT 
    login,
    password,
    nickname,
    registration_date
FROM UserDuplicates
WHERE rn = 1;
  
-- жанры и количество юзеров играющих в них
SELECT 
  pgg.genre_name,
  COUNT(DISTINCT ug.user_login) as user_count
FROM library.users_games ug
JOIN library.publishers_games_genres pgg ON ug.game_id = pgg.game_id
GROUP BY pgg.genre_name
ORDER BY user_count DESC;
