-- 1
copy (select array_to_json(array_agg(row_to_json(u))) from library.users u)
to '/var/lib/postgresql/data/users.json';

copy (select array_to_json(array_agg(row_to_json(g))) from library.games g)
to '/var/lib/postgresql/data/games.json';

copy (select array_to_json(array_agg(row_to_json(ge))) from library.genres ge)
to '/var/lib/postgresql/data/genres.json';

copy (select array_to_json(array_agg(row_to_json(p))) from library.publishers p)
to '/var/lib/postgresql/data/publishers.json';

copy (select array_to_json(array_agg(row_to_json(ug))) from library.users_games ug)
to '/var/lib/postgresql/data/users_games.json';

copy (select array_to_json(array_agg(row_to_json(pg))) from library.publishers_games_genres pg)
to '/var/lib/postgresql/data/publishers_games_genres.json';

select row_to_json(u) from library.users u;
select row_to_json(g) from library.games g;
select row_to_json(ge) from library.genres ge;
select row_to_json(p) from library.publishers p;
select row_to_json(ug) from library.users_games ug;
select row_to_json(pg) from library.publishers_games_genres pg;

-- 2
drop table if exists library.json_table;
create table if not exists library.json_table (
    data jsonb
);

copy library.json_table(data) from '/var/lib/postgresql/data/games_out.json';

select * from library.json_table;

drop table if exists library.games_json;
create table if not exists library.games_json (
    id int primary key,
    name text not null,
    description text,
    price money
);

insert into library.games_json
select
    (data->>'id')::int,
    data->>'name',
    data->>'description',
    (data->>'price')::money
from library.json_table;

select * from library.games_json;

-- 3
drop table if exists library.games_json_atr;
create table if not exists library.games_json_atr (
    id int primary key,
    name text not null,
    game_data jsonb
);

insert into library.games_json_atr values
    (1, 'arc raiders', '{"developer": "embark studios", "prices": {"price": 50.0, "copies": 231241}}'),
    (2, 'arc raiders playtest', '{"developer": "embark studios", "prices": {"price": 0.0, "copies": 3}}'),
    (3, 'battlefied 6', null);

update library.games_json_atr
set game_data = '{"developer": "embark studios", "prices": {"price": 50.0, "copies": 231241}}'
where id = 1;

select * from library.games_json_atr;

-- 4
select
    name,
    game_data->>'developer' as developer,
    game_data->'prices' as prices
from library.games_json_atr;

-- extract
select
    name,
    game_data->>'developer' as developer,
    game_data#>>'{prices,price}' as price
from library.games_json_atr;

-- attr exist
select * from library.games_json_atr where game_data is not null;
select * from library.games_json_atr where game_data->>'prices' is not null;

-- change
update library.games_json_atr
set game_data = '{"developer": "bsg", "prices": {"price": 100, "copies": 1}}'
where game_data is null;

-- split
drop table if exists library.json_table;
create table if not exists library.json_table (
    data jsonb
);

copy library.json_table(data) from '/var/lib/postgresql/data/users.json';

select jsonb_array_elements(data) from library.json_table;

-- custom

create or replace function get_genre_by_restriction(restriction int)
returns table (
  name varchar,
  description text
)
language plpgsql
as $$
begin
  return query
    select g.name, g.description
    from library.genres g
    where g.age_restriction >= restriction;
end
$$;

copy (select array_to_json(array_agg(row_to_json(g))) from get_genre_by_restriction(18) g)
  to '/var/lib/postgresql/data/restriction.json';

-- or
drop procedure get_genre_json_by_restriction_proc;
create or replace procedure get_genre_json_by_restriction_proc(restriction int)
language plpgsql
as $$
begin
  copy (
    select array_to_json(array_agg(row_to_json(d)))
    from (
      select g.name, g.description
      from library.genres g
      where g.age_restriction >= restriction
    ) as d
  )
  to '/var/lib/postgresql/data/restriction.json';
end;
$$;


call get_genre_json_by_restriction_proc(18)
