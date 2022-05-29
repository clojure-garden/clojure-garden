CREATE TABLE IF NOT EXISTS tweet (
	id                UUID,
	content           TEXT NOT NULL,
	created_at        TIMESTAMP NOT NULL,
	author_id         UUID NOT NULL,
	retweet_count     BIGINT DEFAULT 0,
  reply_count       BIGINT DEFAULT 0,
  like_count        BIGINT DEFAULT 0,
  quote_count       BIGINT DEFAULT 0,
	PRIMARY KEY       (id),
  FOREIGN KEY       (author_id) REFERENCES twitter_user(id)
);
--;;
CREATE UNIQUE INDEX tweet_content_created_at_idx on tweet(content, created_at);
--;;
ALTER TABLE tweet
ADD CONSTRAINT unique_tweet_content_created_at
UNIQUE
USING INDEX tweet_content_created_at_idx;
