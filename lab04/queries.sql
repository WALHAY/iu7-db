CREATE EXTENSION plpython3u;

-- scalar
CREATE OR REPLACE FUNCTION get_price_by_id(game_id int)
RETURNS MONEY
LANGUAGE 'plpython3u'
AS $$
  res = plpy.execute("""
  SELECT *
  FROM library.games""")
  return res[game_id]['price']
$$;

SELECT get_price_by_id(12) as PRICE

-- agreg
CREATE OR REPLACE FUNCTION get_avg_genre_price(genre varchar)
RETURNS MONEY
LANGUAGE 'plpython3u'
AS $$
  res = plpy.execute("""
  SELECT *
  FROM library.games g
  JOIN library.publishers_games_genres pgg on pgg.game_id = g.id""")

  c = 0
  s = 0
  for i in res:
    if i['genre_name'] == genre:
      c += 1
      s += float(i['price'].lstrip('$'))

  return s / c if c != 0 else 0
$$;

SELECT get_avg_genre_price('Shooter') as PRICE

-- table
CREATE OR REPLACE FUNCTION get_genre_by_restriction(restriction int)
RETURNS TABLE (
    name varchar,
    description text
)
LANGUAGE 'plpython3u'
AS $$
  res = plpy.execute('SELECT name, description, age_restriction FROM library.genres')

  for row in res:
    if row['age_restriction'] == restriction:
      yield row['name'], row['description']
$$;

SELECT * FROM get_genre_by_restriction('18')

-- proc
CREATE OR REPLACE PROCEDURE adjust_price(percent int)
LANGUAGE 'plpython3u'
AS $$
  inc_price = plpy.prepare('UPDATE library.games SET price = price + ($1 * price / 100)', ['INT'])
  plpy.execute(inc_price, [percent])
$$;

CALL adjust_price(5);

-- trigger
CREATE OR REPLACE FUNCTION instead_of_deletion()
RETURNS TRIGGER
LANGUAGE plpython3u
AS $$
plan = plpy.prepare("""
  UPDATE library.users
  SET nickname = 'Deleted'
  WHERE login = $1""", ["VARCHAR"])

plpy.execute(plan, [TD['old']['login']])
return None
$$;

CREATE OR REPLACE VIEW library.users_view AS
SELECT *
FROM library.users;

CREATE TRIGGER user_deletion
    INSTEAD OF DELETE ON library.users_view
    FOR EACH ROW
    EXECUTE FUNCTION instead_of_deletion();

DELETE FROM library.users_view
WHERE login = 'aaronbyrd';

-- type
CREATE TYPE library.genre_stats AS (
    genre_name text,
    games_count bigint,
    avg_price numeric,
    singleplayer_games bigint
);

CREATE OR REPLACE FUNCTION get_genre_stats()
RETURNS SETOF library.genre_stats
LANGUAGE plpython3u
AS $$
query = '''
    SELECT 
        g.name as genre_name,
        COUNT(pgg.game_id) as games_count,
        ROUND(AVG(ga.price::numeric), 2) as avg_price,
        COUNT(CASE WHEN g.singleplayer = true THEN 1 END) as singleplayer_games
    FROM library.genres g
    LEFT JOIN library.publishers_games_genres pgg ON g.name = pgg.genre_name
    LEFT JOIN library.games ga ON pgg.game_id = ga.id
    GROUP BY g.name
    ORDER BY games_count DESC;
'''

res = plpy.execute(query)

if res is not None:
    return res
$$;

SELECT * from get_genre_stats()

-- custom
CREATE OR REPLACE PROCEDURE generate_random_user()
LANGUAGE 'plpython3u'
AS $$
  import random
  import string
  
  ins = plpy.prepare('INSERT INTO library.users VALUES ($1, $2, $3, CURRENT_DATE)', ['VARCHAR', 'VARCHAR', 'VARCHAR'])

  l = 10
  characters = string.ascii_letters + string.digits
  def random_string(chars, length):
    return ''.join(random.choice(characters) for _ in range(length))

  login = random_string(characters, l)
  password = random_string(characters, l)
  nickname = random_string(characters, l)

  plpy.execute(ins, [login, password, nickname])
$$;

CALL generate_random_user()
