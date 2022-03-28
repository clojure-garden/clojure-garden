CREATE TABLE IF NOT EXISTS language (
	id          UUID,
	name        VARCHAR UNIQUE NOT NULL,
	color       CHAR(7),
	PRIMARY KEY (id)
);
