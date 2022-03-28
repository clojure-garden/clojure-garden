CREATE TABLE IF NOT EXISTS issue (
    id            UUID,
    title         TEXT NOT NULL,
    created_at    TIMESTAMP NOT NULL,
    url           TEXT NOT NULL,
    closed        BOOLEAN NOT NULL,
    closed_at     TIMESTAMP,
    repository_id UUID NOT NULL,
    PRIMARY KEY   (id),
    FOREIGN KEY   (repository_id) REFERENCES repository(id)
);
