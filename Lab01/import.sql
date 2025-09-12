COPY library.users
	FROM '/var/lib/postgresql/data/users.csv'
	DELIMITER ',' CSV HEADER;

COPY library.games
	FROM '/var/lib/postgresql/data/games.csv'
	DELIMITER ',' CSV HEADER;

COPY library.publishers
	FROM '/var/lib/postgresql/data/publishers.csv'
	DELIMITER ',' CSV HEADER;

COPY library.genres
	FROM '/var/lib/postgresql/data/genres.csv'
	DELIMITER ',' CSV HEADER;

COPY library.users_games
	FROM '/var/lib/postgresql/data/users_games.csv'
	DELIMITER ',' CSV HEADER;

COPY library.publishers_games_genres
	FROM '/var/lib/postgresql/data/publishers_games_genres.csv'
	DELIMITER ',' CSV HEADER;
