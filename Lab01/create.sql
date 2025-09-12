CREATE SCHEMA library;

CREATE TABLE IF NOT EXISTS library.users (
	id SERIAL,
	login VARCHAR,
	password VARCHAR,
	name VARCHAR,
	registration_date DATE
);

CREATE TABLE IF NOT EXISTS library.games (
	id SERIAL,
	name VARCHAR,
	description VARCHAR,
	price MONEY
);

CREATE TABLE IF NOT EXISTS library.genres ( 
	id SERIAL,
	name VARCHAR,
	description VARCHAR
);

CREATE TABLE IF NOT EXISTS library.publishers (
	id SERIAL,
	name VARCHAR,
	site VARCHAR,
	description VARCHAR,
	rating NUMERIC,
	country VARCHAR
);

CREATE TABLE IF NOT EXISTS library.users_games (
    id SERIAL,
	user_id INT,
	game_id INT,
	purchase_date DATE
);

CREATE TABLE IF NOT EXISTS library.publishers_games_genres (
    id SERIAL,
	publisher_id INT,
	game_id INT,
	genre_id INT,
	publish_date DATE
);
