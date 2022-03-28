CREATE TABLE IF NOT EXISTS asset (
	id           UUID,
	name         VARCHAR NOT NULL,
	downloads    BIGINT DEFAULT 0,
	release_id   UUID NOT NULL,
	PRIMARY KEY  (id),
	FOREIGN KEY  (release_id) REFERENCES release(id),
	UNIQUE       (name, release_id)
);
