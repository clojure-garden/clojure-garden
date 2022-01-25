(ns platform.github.github-ctl
  (:require
    [clojure.core.async :as async]
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    [platform.github.api-client :as github-client]
    [platform.github.github-sql :as github-sql]
    [platform.utils.seeder :as seeder]
    [platform.utils.tagger :as tagger])
  (:import
    (clojure.lang
      ExceptionInfo)))


(defn- already-pulled?
  [url db]
  (if-let [[repo-owner repo-name] (github-client/parse-repository-url url)]
    (github-sql/repository-exists? db repo-owner repo-name)
    (throw (ex-info (str "Invalid GitHub repository URL: " url)
                    {:url url}))))


(defn pull-repository-info
  [db url tags]
  (try
    (log/info "Pulling GitHub repository statistics:" url)
    (if-not (already-pulled? url db)
      (let [{:as repo-info :keys [name-with-owner topics]} (github-client/get-repository-info url)
            [_ repo-name] (str/split name-with-owner #"/")
            {:keys [categories platforms]} (get tags (keyword (str/lower-case repo-name)))]
        (->> (concat topics categories platforms)
             (distinct)
             (vec)
             (assoc repo-info :topics)
             (github-sql/insert-repository-info! db))
        (log/info "The repository" url "statistics pulled!"))
      (log/warn "The repository" url "is already pulled! Skipping..."))
    (catch ExceptionInfo ex (log/error (ex-message ex) "\n" (ex-data ex)))
    (catch Exception ex (log/error (.getMessage ex)))))


(defn pull-repository-info-handler
  [req]
  (async/thread
    (let [{{:keys [datasource]} :db
           {:keys [url]} :params} req]
      (->> (tagger/read-clojure-toolbox-data)
           (pull-repository-info datasource url))))
  {:status 202})


(defn pull-repository-info-all-handler
  [req]
  (async/thread
    (log/info "Getting a list of GitHub URLs...")
    (let [datasource (get-in req [:platform.system/ctx :db :datasource])
          project-urls (seeder/get-repository-urls datasource)
          project-tags (tagger/read-clojure-toolbox-data)]
      (log/info (count project-urls) "GitHub URLs will be fetched")
      (doall (map (fn [url]
                    (pull-repository-info datasource url project-tags)
                    (Thread/sleep 10000))
                  project-urls))))
  {:status 202})
