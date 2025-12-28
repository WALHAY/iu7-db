-- scalar func
CREATE OR REPLACE FUNCTION get_price_by_id(game_id int)
RETURNS MONEY
LANGUAGE PLPGSQL
AS $$
BEGIN
    RETURN (SELECT g.price
    FROM library.games g
    WHERE g.id = game_id);
END
$$;

SELECT get_price_by_id(5) as price;

-- table func
CREATE OR REPLACE FUNCTION get_genre_by_restriction(restriction int)
RETURNS TABLE (
  name varchar,
  description TEXT
)
LANGUAGE PLPGSQL
AS $$
BEGIN
  RETURN QUERY
    SELECT g.name, g.description
    FROM library.genres g
    WHERE g.age_restriction = restriction;
END
$$;

SELECT * FROM get_genre_by_restriction(18);

-- multiop func
CREATE OR REPLACE FUNCTION get_user_detailed_stats(user_login VARCHAR)
RETURNS TABLE(
    total_games BIGINT,
    total_spent MONEY,
    favorite_genre VARCHAR
) 
LANGUAGE PLPGSQL
AS $$
BEGIN
    RETURN QUERY
    WITH user_stats AS (
        SELECT 
            COUNT(*) as total_games,
            COALESCE(SUM(g.price), 0::MONEY) as total_spent
        FROM library.users_games ug
        JOIN library.games g ON ug.game_id = g.id
        WHERE ug.user_login = get_user_detailed_stats.user_login
    ),
    top_genre AS (
        SELECT 
            pgg.genre_name as favorite_genre
        FROM library.publishers_games_genres pgg
        JOIN library.users_games ug ON pgg.game_id = ug.game_id
        WHERE ug.user_login = get_user_detailed_stats.user_login
        GROUP BY pgg.genre_name
        ORDER BY COUNT(*) DESC
        LIMIT 1
    )
    SELECT 
        us.total_games,
        us.total_spent,
        tg.favorite_genre
    FROM user_stats us
    CROSS JOIN top_genre tg;
END
$$;

SELECT * FROM get_user_detailed_stats('adiaz');

-- rec func 
CREATE OR REPLACE FUNCTION get_daily_stats_forward(
    start_date DATE DEFAULT CURRENT_DATE - 6,
    days_count INT DEFAULT 7
)
RETURNS TABLE(
    stat_date DATE,
    purchases_count BIGINT,
    daily_revenue MONEY
) 
LANGUAGE PLPGSQL
AS $$
BEGIN
    RETURN QUERY
    WITH RECURSIVE date_sequence AS (
        SELECT 
            start_date as generated_date, 
            1 as day_number
        
        UNION ALL
        
        SELECT 
            generated_date + 1, 
            day_number + 1
        FROM date_sequence
        WHERE day_number < days_count
    )
    SELECT 
        ds.generated_date,
        COUNT(ug.purchase_date),
        COALESCE(SUM(g.price::numeric), 0)::money
    FROM date_sequence ds
    LEFT JOIN library.users_games ug ON ug.purchase_date = ds.generated_date
    LEFT JOIN library.games g ON ug.game_id = g.id
    GROUP BY ds.generated_date
    ORDER BY ds.generated_date ASC;
END;
$$;

SELECT * FROM get_daily_stats_forward('10-10-2024');

-- proc
CREATE OR REPLACE PROCEDURE adjust_price(percent int)
LANGUAGE PLPGSQL
AS $$
BEGIN
    UPDATE library.games
    SET price = price + (percent * price / 100);
END
$$;

CALL adjust_price(5);

-- proc rec
CREATE OR REPLACE PROCEDURE adjust_price_range(startid int, games int, percent int)
LANGUAGE PLPGSQL
AS $$
BEGIN
  UPDATE library.games
  SET price = price + (percent * price / 100)
  WHERE id = startid;

  if games > 1 then
    call adjust_price_range(startid + 1, games - 1, percent);
  end if;
END
$$;

CALL adjust_price_range(0, 5, 1);

-- proc cursor
CREATE OR REPLACE PROCEDURE analyze_user_purchases(
    login VARCHAR
)
LANGUAGE PLPGSQL
AS $$
DECLARE
    purchase_record RECORD;
    total_games INT := 0;
    total_spent MONEY := 0;

    user_purchases_cursor CURSOR FOR 
        SELECT 
            g.name as game_name,
            g.price,
            ug.purchase_date,
            pgg.genre_name,
            pgg.publisher_name
        FROM library.users_games ug
        JOIN library.games g ON ug.game_id = g.id
        JOIN library.publishers_games_genres pgg ON ug.game_id = pgg.game_id
        WHERE ug.user_login = login
        ORDER BY ug.purchase_date DESC;
BEGIN
    RAISE LOG 'Анализ покупок пользователя: %', login;
    
    OPEN user_purchases_cursor;
    
    LOOP
        FETCH user_purchases_cursor INTO purchase_record;
        EXIT WHEN NOT FOUND;
        
        total_games := total_games + 1;
        total_spent := total_spent + purchase_record.price;
        
        RAISE LOG 'Игра: %', purchase_record.game_name;
        RAISE LOG '    Цена: %, Дата: %, Жанр: %, Издатель: %',
            purchase_record.price,
            purchase_record.purchase_date,
            purchase_record.genre_name,
            purchase_record.publisher_name;
    END LOOP;
    
    CLOSE user_purchases_cursor;
    
    RAISE LOG 'Игр: %, Общая сумма: %', total_games, total_spent; 
END;
$$;

CALL analyze_user_purchases('adiaz');

-- procedure with metadata
CREATE OR REPLACE PROCEDURE get_meta(tablename varchar)
LANGUAGE PLPGSQL
AS $$
declare
  column_record RECORD;
BEGIN
  RAISE LOG 'Таблица %', tablename;
  FOR column_record IN
      SELECT 
          column_name,
          data_type
      FROM information_schema.columns
      WHERE table_schema = 'library' 
        AND table_name = get_meta.tablename
      ORDER BY ordinal_position
  LOOP
      RAISE LOG '%: %',
              column_record.column_name,
              column_record.data_type;
  END LOOP;
END
$$;

CALL get_meta('users');

-- after trigger
CREATE OR REPLACE FUNCTION track_price_change()
RETURNS TRIGGER
LANGUAGE PLPGSQL
AS $$
BEGIN
    IF OLD.price != NEW.price THEN
        RAISE LOG 'Изменение цены "%": была %, стала %', 
            OLD.name, 
            OLD.price, 
            NEW.price;
    END IF;
    
    RETURN NEW;
END;
$$;

CREATE TRIGGER after_price_update
    AFTER UPDATE ON library.games
    FOR EACH ROW
    EXECUTE FUNCTION track_price_change();

-- instead of trigger
CREATE OR REPLACE FUNCTION instead_of_deletion()
RETURNS TRIGGER
LANGUAGE PLPGSQL
AS $$
BEGIN
    UPDATE library.users
    SET nickname = 'Deleted'
    WHERE login = OLD.login;

    RETURN OLD;
END;
$$;

CREATE OR REPLACE VIEW library.users_view AS
SELECT *
FROM library.users;

CREATE TRIGGER user_deletion
    INSTEAD OF DELETE ON library.users_view
    FOR EACH ROW
    EXECUTE FUNCTION instead_of_deletion();

DELETE FROM library.users_view
WHERE login = 'adam69';

-- custom
DROP function find_new_games;
CREATE OR REPLACE FUNCTION find_new_games(login varchar)
RETURNS TABLE (
  name varchar,
  price money,
  publish_date date,
  register_date date
)
LANGUAGE PLPGSQL
AS $$
DECLARE
  user_reg_date date;
BEGIN
  SELECT registration_date INTO user_reg_date
    FROM library.users u
    WHERE u.login = find_new_games.login;
  
  RETURN QUERY
  SELECT g.name, g.price, pgg.publish_date, user_reg_date
    FROM library.publishers_games_genres pgg
    JOIN library.games g ON pgg.game_id = g.id
    WHERE pgg.publish_date > user_reg_date
      AND g.id NOT IN (SELECT ug.id
                          FROm library.users_games ug
                          WHERE ug.user_login = find_new_games.login)
    ORDER BY pgg.publish_date ASC;
END
$$;

SELECT * FROM find_new_games('adam58');
