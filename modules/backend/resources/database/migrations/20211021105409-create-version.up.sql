CREATE TABLE IF NOT EXISTS version (
	id           UUID,
	name         VARCHAR NOT NULL,
	downloads    bigint NOT NULL DEFAULT 0,
	library_id   UUID NOT NULL,
	PRIMARY KEY  (id),
	FOREIGN KEY  (library_id) REFERENCES library(id),
	UNIQUE       (name, library_id)
);
