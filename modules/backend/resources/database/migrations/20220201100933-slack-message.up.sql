CREATE TABLE IF NOT EXISTS slack_message (
	id                UUID,
	content           TEXT NOT NULL,
	created_at        TIMESTAMP NOT NULL,
	channel_id        TEXT,
	permalink         TEXT,
	user_id           TEXT,
	PRIMARY KEY       (id),
	UNIQUE            (content, created_at)
);
