CREATE TABLE IF NOT EXISTS library_repository (
  library_id    UUID,
  repository_id UUID,
  PRIMARY KEY   (library_id, repository_id),
  FOREIGN KEY   (library_id) REFERENCES library(id),
  FOREIGN KEY   (repository_id) REFERENCES repository(id)
);
