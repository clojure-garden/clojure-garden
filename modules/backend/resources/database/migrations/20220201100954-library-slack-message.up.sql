CREATE TABLE IF NOT EXISTS library_slack_message (
  library_id          UUID,
  slack_message_id    UUID,
	PRIMARY KEY         (library_id, slack_message_id),
	FOREIGN KEY         (library_id) REFERENCES library(id),
	FOREIGN KEY         (slack_message_id) REFERENCES slack_message(id)
);
