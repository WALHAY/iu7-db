CREATE SCHEMA library;

CREATE TABLE IF NOT EXISTS library.users (
	login VARCHAR,
	password VARCHAR,
	nickname VARCHAR,
	registration_date DATE
);

CREATE TABLE IF NOT EXISTS library.games (
	id SERIAL,
	name VARCHAR,
	description TEXT,
	price MONEY
);

CREATE TABLE IF NOT EXISTS library.genres ( 
	name VARCHAR,
	description TEXT,
	singleplayer BOOLEAN,
	age_restriction INT
);

CREATE TABLE IF NOT EXISTS library.publishers (
	name VARCHAR,
	site VARCHAR,
	description VARCHAR,
	rating NUMERIC,
	country VARCHAR
);

CREATE TABLE IF NOT EXISTS library.users_games (
	id SERIAL,
	user_login VARCHAR,
	game_id INT,
	purchase_date DATE
);

CREATE TABLE IF NOT EXISTS library.publishers_games_genres (
	id SERIAL,
	publisher_name VARCHAR,
	game_id INT,
	genre_name VARCHAR,
	publish_date DATE
);
