CREATE TABLE IF NOT EXISTS version_dependency (
	version_id    UUID,
	dependency_id UUID,
	PRIMARY KEY   (version_id, dependency_id),
	FOREIGN KEY   (version_id) REFERENCES version(id),
	FOREIGN KEY   (dependency_id) REFERENCES version(id)
);
