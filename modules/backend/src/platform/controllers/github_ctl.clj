(ns platform.controllers.github-ctl
  (:require
    [clojure.core.async :as async]
    [clojure.tools.logging :as log]
    [platform.client.github :as github-client]
    [platform.models.github-sql :as github-sql]
    [platform.utils.seeder :as seeder]
    [ring.util.response :as resp])
  (:import
    (clojure.lang
      ExceptionInfo)))


(defn- already-pulled?
  [url datasource]
  (if-let [[repo-owner repo-name] (github-client/parse-repository-url url)]
    (github-sql/repository-exists? datasource repo-owner repo-name)
    (throw (ex-info (str "Invalid GitHub repository URL: " url)
                    {:url url}))))


(defn pull-repository-info
  [url datasource]
  (try
    (log/info "Pulling GitHub repository statistics:" url)
    (if-not (already-pulled? url datasource)
      (let [repo-info (github-client/get-repository-info url)]
        (github-sql/insert-repository-info! datasource repo-info)
        (log/info "The repository" url "statistics pulled!"))
      (log/warn "The repository" url "is already pulled! Skipping..."))
    (catch ExceptionInfo ex (log/error (ex-message ex) "\n" (ex-data ex)))
    (catch Exception ex (log/error (.getMessage ex)))))


(defn pull-repository-info-handler
  [req]
  (async/thread
    (let [{{:keys [datasource] :as db-spec} :db
           {:keys [url]} :params} req]
      (pull-repository-info url datasource)))
  (resp/redirect "/status"))


(defn pull-repository-info-all-handler
  [req]
  (async/thread
    (log/info "Getting a list of GitHub URLs...")
    (let [{{:keys [datasource] :as db-spec} :db} req
          project-urls (seeder/get-repository-urls datasource)]
      (log/info (count project-urls) "GitHub URLs will be fetched")
      (doall (map (fn [url]
                    (pull-repository-info url datasource)
                    (Thread/sleep 10000))
                  project-urls))))
  (resp/redirect "/status"))


