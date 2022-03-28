CREATE TABLE IF NOT EXISTS repository_language (
    repository_id          UUID,
    language_id            UUID,
    size                   INTEGER NOT NULL,
    is_primary_language    BOOLEAN NOT NULL,
	PRIMARY KEY              (repository_id, language_id),
	FOREIGN KEY              (repository_id) REFERENCES repository(id),
	FOREIGN KEY              (language_id) REFERENCES language(id)
);
