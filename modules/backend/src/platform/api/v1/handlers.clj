(ns platform.api.v1.handlers
  (:require
    [platform.models.github-sql :as github-sql]))


(defn get-repository-info-all-handler
  [req]
  (let [datasource (get-in req [:platform.system/ctx :db :datasource])]
    {:status 200
     :body (pr-str (github-sql/select-repositories datasource))}))

