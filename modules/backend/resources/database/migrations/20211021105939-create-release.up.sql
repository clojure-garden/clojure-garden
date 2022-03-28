CREATE TABLE IF NOT EXISTS release (
	id            UUID,
	name          VARCHAR,
	tag_name      VARCHAR NOT NULL,
	created_at    TIMESTAMP,
	downloads     BIGINT DEFAULT 0,
	repository_id UUID NOT NULL,
	PRIMARY KEY   (id),
	FOREIGN KEY   (repository_id) REFERENCES repository(id),
	UNIQUE        (tag_name, repository_id)
);
