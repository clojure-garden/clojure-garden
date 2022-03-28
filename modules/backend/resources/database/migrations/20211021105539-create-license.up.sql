CREATE TABLE IF NOT EXISTS license (
	id                UUID,
	name              VARCHAR UNIQUE NOT NULL,
	nick_name         VARCHAR UNIQUE,
	url               TEXT,
	is_pseudo_license BOOLEAN,
	PRIMARY KEY       (id)
);
