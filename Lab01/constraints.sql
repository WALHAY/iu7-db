ALTER TABLE library.users
	ADD PRIMARY KEY (login);
ALTER TABLE library.games
	ADD PRIMARY KEY (id);
ALTER TABLE library.genres
	ADD PRIMARY KEY (name);
ALTER TABLE library.publishers
	ADD PRIMARY KEY (name);
ALTER TABLE library.users_games
	ADD PRIMARY KEY (id);
ALTER TABLE library.publishers_games_genres
	ADD PRIMARY KEY (id);

ALTER TABLE library.users
	ALTER COLUMN login SET NOT NULL;
ALTER TABLE library.users
	ALTER COLUMN password SET NOT NULL;
ALTER TABLE library.users
	ALTER COLUMN nickname SET NOT NULL;
ALTER TABLE library.users
	ALTER COLUMN registration_date SET NOT NULL;

ALTER TABLE library.games
	ALTER COLUMN name SET NOT NULL;
ALTER TABLE library.games
	ALTER COLUMN price SET NOT NULL;

ALTER TABLE library.genres
	ALTER COLUMN name SET NOT NULL;

ALTER TABLE library.publishers
	ALTER COLUMN name SET NOT NULL;
ALTER TABLE library.publishers
	ALTER COLUMN country SET NOT NULL;

ALTER TABLE library.users_games
	ALTER COLUMN purchase_date SET NOT NULL;

ALTER TABLE library.publishers_games_genres
	ALTER COLUMN publish_date SET NOT NULL;

ALTER TABLE library.games
	ADD CONSTRAINT games_price_non_negative
	CHECK (price >= 0::money);
	
ALTER TABLE library.publishers
	ADD CONSTRAINT publishers_rating_range
	CHECK (rating >= 0 AND rating <= 5);

ALTER TABLE library.users_games 
    ADD CONSTRAINT fk_users_games_user_id
		FOREIGN KEY (user_login)
			REFERENCES library.users(login)
		ON UPDATE CASCADE
		ON DELETE CASCADE,
    ADD CONSTRAINT fk_users_games_game_id
		FOREIGN KEY (game_id)
			REFERENCES library.games(id)
		ON UPDATE CASCADE
		ON DELETE CASCADE;

ALTER TABLE library.publishers_games_genres 
    ADD CONSTRAINT fk_publishers_games_genres_publisher_id
		FOREIGN KEY (publisher_name)
			REFERENCES library.publishers(name)
		ON UPDATE CASCADE
		ON DELETE CASCADE,
    ADD CONSTRAINT fk_publishers_games_genres_game_id
		FOREIGN KEY (game_id)
			REFERENCES library.games(id)
		ON UPDATE CASCADE
		ON DELETE CASCADE,
    ADD CONSTRAINT fk_publishers_games_genres_genre_id
		FOREIGN KEY (genre_name)
			REFERENCES library.genres(name)
		ON UPDATE CASCADE
		ON DELETE CASCADE;
