CREATE TABLE IF NOT EXISTS version_license (
	version_id  UUID,
	license_id  UUID,
	PRIMARY KEY (version_id, license_id),
	FOREIGN KEY (version_id) REFERENCES version (id),
	FOREIGN KEY (license_id) REFERENCES license (id)
);
