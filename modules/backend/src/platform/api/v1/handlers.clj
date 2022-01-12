(ns platform.api.v1.handlers
  (:require
    [platform.github.github-sql :as github-sql]
    [platform.github.repository :as github-repo]))


(defn get-repository-info-all-handler
  [req]
  (let [datasource (get-in req [:platform.system/ctx :db :datasource])]
    {:status 200
     :body (-> (github-sql/select-repositories datasource)
               (github-repo/preprocess-repositories))}))

