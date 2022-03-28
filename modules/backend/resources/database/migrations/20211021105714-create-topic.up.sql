CREATE TABLE IF NOT EXISTS topic (
	id          UUID,
	name        VARCHAR UNIQUE NOT NULL,
	PRIMARY KEY (id)
);
