CREATE TABLE IF NOT EXISTS tweet (
	id                UUID,
	content           TEXT NOT NULL,
	created_at        TIMESTAMP NOT NULL,
	author_id         TEXT,
	retweet_count     BIGINT DEFAULT 0,
  reply_count       BIGINT DEFAULT 0,
  like_count        BIGINT DEFAULT 0,
  quote_count       BIGINT DEFAULT 0,
	PRIMARY KEY       (id)
);
--;;
CREATE UNIQUE INDEX tweet_content_created_at_idx on tweet(content, created_at);
--;;
ALTER TABLE tweet
ADD CONSTRAINT unique_tweet_content_created_at
UNIQUE
USING INDEX tweet_content_created_at_idx;
