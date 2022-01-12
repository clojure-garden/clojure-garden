(ns platform.api.v1.handlers
  (:require
    [platform.github.github-sql :as github-sql]
    [platform.github.repository.preprocess :as repo-prep]))


(defn get-repository-info-all-handler
  [req]
  (let [datasource (get-in req [:platform.system/ctx :db :datasource])]
    {:status 200
     :body (-> (github-sql/select-repositories datasource)
               (repo-prep/preprocess-repositories))}))

