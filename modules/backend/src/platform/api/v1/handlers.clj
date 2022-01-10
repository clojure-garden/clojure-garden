(ns platform.api.v1.handlers
  (:require
    [platform.models.github-sql :as github-sql]))


(defn get-repository-info-all-handler
  [req]
  (let [{{:keys [datasource]} :db} req]
    {:status 200
     :body (pr-str (github-sql/select-repositories datasource))}))

