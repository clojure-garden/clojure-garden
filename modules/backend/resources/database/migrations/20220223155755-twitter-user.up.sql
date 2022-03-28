CREATE TABLE IF NOT EXISTS twitter_user (
	id                UUID,
	id_str            TEXT NOT NULL,
	name              VARCHAR NOT NULL,
	screen_name       VARCHAR NOT NULL,
	location          TEXT,
	url               TEXT,
	description       TEXT,
	verified          BOOLEAN NOT NULL,
	created_at        TIMESTAMP NOT NULL,
	followers_count   BIGINT DEFAULT 0,
	following_count   BIGINT DEFAULT 0,
	listed_count      BIGINT DEFAULT 0,
	tweet_count       BIGINT DEFAULT 0,
	PRIMARY KEY       (id),
	UNIQUE            (screen_name)
);
