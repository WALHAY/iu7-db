-- rk2

-- 1
create schema if not exists rk2;

create table if not exists rk2.excursions (
    id serial primary key,
    name varchar(255) not null,
    description text,
    date_open date not null,
    date_close date not null
);

insert into rk2.excursions(name, description, date_open, date_close)
values
    ('exc a', 'a excursion description', '2025-09-01', '2025-09-02'),
    ('exc b', 'b excursion description', '2025-09-02', '2025-09-03'),
    ('exc c', 'c excursion description', '2025-09-03', '2025-09-04'),
    ('exc d', 'd excursion description', '2025-09-04', '2025-09-05'),
    ('exc e', 'e excursion description', '2025-09-05', '2025-09-06'),
    ('exc f', 'f excursion description', '2025-09-06', '2025-09-07'),
    ('exc g', 'g excursion description', '2025-09-07', '2025-09-08'),
    ('exc h', 'h excursion description', '2025-09-08', '2025-09-09'),
    ('exc i', 'i excursion description', '2025-09-09', '2025-09-10'),
    ('exc j', 'j excursion description', '2025-09-10', '2025-09-11');

create table if not exists rk2.visitors (
    id serial primary key,
    fio varchar(255) not null,
    address varchar(255),
    phone varchar(20)
);

insert into rk2.visitors(fio, address, phone)
values
    ('vis a b', 'a b vis address', '8-800-555-35-35'),
    ('vis b c', 'b c vis address', '8-800-555-35-36'),
    ('vis c d', 'c d vis address', '8-800-555-35-37'),
    ('vis d e', 'd e vis address', '8-800-555-35-38'),
    ('vis e f', 'e f vis address', '8-800-555-35-39'),
    ('vis f g', 'f g vis address', '8-800-555-35-40'),
    ('vis g h', 'g h vis address', '8-800-555-35-41'),
    ('vis h i', 'h i vis address', '8-800-555-35-42'),
    ('vis i j', 'i j vis address', '8-800-555-35-43'),
    ('vis j k', 'j k vis address', '8-800-555-35-44');

create table if not exists rk2.stands (
    id serial primary key,
    name varchar(255) not null,
    subject varchar(100),
    description text
);

insert into rk2.stands(name, subject, description)
values
    ('stand a', 'science', 'science stand'),
    ('stand b', 'philosophy', 'b philosophy stand'),
    ('stand c', 'science', 'c science stand'),
    ('stand d', 'art', 'd art stand'),
    ('stand e', 'art', 'e art stand'),
    ('stand f', 'science', 'f science stand'),
    ('stand g', 'philosophy', 'philosophy g stand'),
    ('stand h', 'science', 'h science stand'),
    ('stand i', 'music', 'i music stand'),
    ('stand j', 'music', 'j music stand');

create table if not exists rk2.excursion_visitor (
    id serial primary key,
    excursion_id int not null,
    visitor_id int not null,
    visit_date date not null,
    constraint fk_excursion foreign key (excursion_id) references rk2.excursions(id) on delete cascade,
    constraint fk_visitor foreign key (visitor_id) references rk2.visitors(id) on delete cascade,
    constraint unique_visit unique (excursion_id, visitor_id, visit_date)
);

insert into rk2.excursion_visitor(excursion_id, visitor_id, visit_date)
values
    (1, 5, '2025-09-01'),
    (3, 6, '2025-09-03'),
    (7, 8, '2025-09-07'),
    (10, 9, '2025-09-10'),
    (5, 3, '2025-09-05'),
    (4, 4, '2025-09-04'),
    (5, 8, '2025-09-05'),
    (8, 9, '2025-09-08'),
    (7, 9, '2025-09-07'),
    (10, 4, '2025-09-10');

create table if not exists rk2.excursion_stand (
    id serial primary key,
    excursion_id int not null,
    stand_id int not null,
    arrive_date date not null,
    constraint fk_excursion foreign key (excursion_id) references rk2.excursions(id) on delete cascade,
    constraint fk_stand foreign key (stand_id) references rk2.stands(id) on delete cascade,
    constraint unique_excursion_stand unique (excursion_id, stand_id)
);

insert into rk2.excursion_stand(excursion_id, stand_id, arrive_date)
values
    (5, 1, '2025-09-01'),
    (6, 3, '2025-09-03'),
    (8, 7, '2025-09-07'),
    (9, 10, '2025-09-08'),
    (3, 5, '2025-09-01'),
    (4, 4, '2025-09-04'),
    (8, 5, '2025-09-05'),
    (9, 8, '2025-09-08'),
    (9, 7, '2025-09-07'),
    (4, 10, '2025-09-03');

-- 2

select min(arrive_date) as arrive_start,
  max(arrive_date) as arrive_end
  from rk2.excursion_stand; -- находит начало и конец прибытия стендов

select 
    v.id,
    v.fio,
    ev.visit_date,
    case 
        when ev.visit_date <= '2025-09-03' then 'Big'
        when ev.visit_date <= '2025-09-07' then 'Medium'
        else 'Small'
    end as prize_size
from rk2.visitors v
join rk2.excursion_visitor ev on ev.visitor_id = v.id
order by ev.visit_date; -- размер подарка в зависимости от дня посещения экскурсии

select d.id, d.name, d.visitor_count
from (
    select
        e.id,
        e.name,
        (select count(*) 
         from rk2.excursion_visitor ev 
         where ev.excursion_id = e.id) as visitor_count
    from rk2.excursions e
) as d
order by d.id; -- количество посетитиелей для каждой экскурсии

-- 3
create or replace function drop_all_dml_triggers()
returns integer as $$
declare
    r record;
    cnt integer := 0;
begin
    for r in
        select trigger_name, event_object_schema, event_object_table
        from information_schema.triggers
        where trigger_schema = current_schema()
    loop
        execute format('drop trigger %I on %I.%I',
            r.trigger_name, r.event_object_schema, r.event_object_table);
        cnt := cnt + 1;
    end loop;
    
    return cnt;
end;
$$ language plpgsql;

select drop_all_dml_triggers();

