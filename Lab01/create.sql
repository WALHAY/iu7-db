CREATE SCHEMA library;

CREATE TABLE IF NOT EXISTS library.users (
	id INT,
	login VARCHAR,
	password VARCHAR,
	name VARCHAR,
	registation_date DATE
);

CREATE TABLE IF NOT EXISTS library.games (
	id INT,
	name VARCHAR,
	description VARCHAR,
	price MONEY
);

CREATE TABLE IF NOT EXISTS library.genres ( 
	id INT,
	name VARCHAR,
	description VARCHAR
);

CREATE TABLE IF NOT EXISTS library.publishers (
	id INT,
	name VARCHAR,
	site VARCHAR,
	description VARCHAR,
	rating NUMERIC,
	country VARCHAR
);

CREATE TABLE IF NOT EXISTS library.users_games (
	user_id INT,
	game_id INT,
	purchase_date DATE
);

CREATE TABLE IF NOT EXISTS library.publishers_games_genres (
	publisher_id INT,
	game_id INT,
	genre_id INT,
	publish_date DATE
);
