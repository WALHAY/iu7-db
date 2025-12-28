create or replace procedure library.get_publishers_count(
    out games_count int
)
language plpgsql
as $$
begin
    select count(*) into games_count
    from library.publishers;
end;
$$;