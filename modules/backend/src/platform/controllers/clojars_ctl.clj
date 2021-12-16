(ns platform.controllers.clojars-ctl
  (:require
    [clojure.core.async :as async]
    [clojure.tools.logging :as log]
    [platform.client.clojars :as clojars-client]
    [platform.models.clojars-sql :as clojars-sql]
    [platform.utils.seeder :as seeder]
    [ring.util.response :as resp])
  (:import
    (clojure.lang
      ExceptionInfo)))


(defn already-pulled?
  [url db]
  (if-let [[group-id artifact-id] (clojars-client/get-package-identifier url)]
    (clojars-sql/artifact-exists? db group-id artifact-id)
    (throw (ex-info (str "Invalid Clojars repository URL: " url)
                    {:url url}))))


(defn pull-artifact-info
  [url db]
  (try
    (log/info "Pulling Clojars artifact statistics:" url)
    (if-not (already-pulled? url db)
      (let [artifact-info (clojars-client/get-artifact-info url)]
        (clojars-sql/insert-artifact-info! db artifact-info true)
        (log/info "The artifact" url "statistics pulled!"))
      (log/warn "The artifact" url "is already pulled! Skipping..."))
    (catch ExceptionInfo ex (log/error (ex-message ex) "\n" (ex-data ex)))
    (catch Exception ex (log/error (.getMessage ex)))))


(defn pull-artifact-info-handler
  [req]
  (let [{{:keys [datasource]} :db
         {:keys [url]} :params} req]
    (pull-artifact-info url datasource)
    (resp/redirect "/status")))


(defn pull-artifact-info-all-handler
  [req]
  (async/thread
    (log/info "Getting a list of Clojars URLs...")
    (let [{{:keys [datasource]} :db} req
          project-urls (seeder/get-artifact-urls)]
      (log/info (count project-urls) "Clojar URLs will be fetched")
      (doall (map (fn [url]
                    (pull-artifact-info url datasource)
                    (Thread/sleep 5000))
                  project-urls))))
  (resp/redirect "/status"))
