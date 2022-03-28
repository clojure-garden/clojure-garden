CREATE TABLE IF NOT EXISTS library_tweet (
  library_id  UUID,
  tweet_id    UUID,
	PRIMARY KEY (library_id, tweet_id),
	FOREIGN KEY (library_id) REFERENCES library(id),
	FOREIGN KEY (tweet_id)   REFERENCES tweet(id)
);
