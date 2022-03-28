CREATE TABLE IF NOT EXISTS library (
	id                UUID,
	artifact_id       VARCHAR NOT NULL,
	group_id          VARCHAR NOT NULL,
	homepage          TEXT,
	description       TEXT,
	owner             VARCHAR NOT NULL,
  latest_version    TEXT,
  latest_release    TEXT,
  downloads         BIGINT NOT NULL DEFAULT 0,
  from_clojars      BOOLEAN NOT NULL,
	PRIMARY KEY (id),
	UNIQUE      (artifact_id, group_id)
);
