(ns platform.github.topics
  (:require
    [platform.github.github-sql :as github-sql]))


(defn get-topics
  [datasource limit offset]
  (github-sql/select-topics datasource limit offset))


(defn count-topics
  [datasource]
  (-> (github-sql/select-topics datasource)
      (first)
      (:aggregate)))
