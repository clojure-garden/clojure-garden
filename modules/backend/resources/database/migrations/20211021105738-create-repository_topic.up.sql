CREATE TABLE IF NOT EXISTS repository_topic (
  repository_id UUID,
  topic_id      UUID,
	PRIMARY KEY   (repository_id, topic_id),
	FOREIGN KEY   (repository_id) REFERENCES repository(id),
	FOREIGN KEY   (topic_id) REFERENCES topic(id)
);
