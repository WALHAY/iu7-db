COPY library.users FROM "/var/lib/postgresql/data/users.csv" DELIMETER ',' CSV HEADER;

COPY library.games FROM "/var/lib/postgresql/games.csv" DELIMETER ',' CSV HEADER;

COPY library.publishers FROM "/var/lib/postgresql/publishers.csv" DELIMETER ',' CSV HEADER;

COPY library.genres FROM "/var/lib/postgresql/genres.csv" DELIMETER ',' CSV HEADER;

COPY library.users FROM "/var/lib/postgresql/users.csv" DELIMETER ',' CSV HEADER;

