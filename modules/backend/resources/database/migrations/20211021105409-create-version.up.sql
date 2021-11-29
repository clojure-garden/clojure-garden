CREATE TABLE IF NOT EXISTS version (
	id           UUID,
	name         VARCHAR NOT NULL,
	downloads    bigint NOT NULL DEFAULT 0,
	artifact_id  UUID NOT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (artifact_id) REFERENCES artifact(id),
	UNIQUE(artifact_id, name)
);
