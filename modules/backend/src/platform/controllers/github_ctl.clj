(ns platform.controllers.github-ctl
  (:require
    [clojure.core.async :as async]
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    [platform.client.github :as github-client]
    [platform.models.github-sql :as github-sql]
    [platform.utils.seeder :as seeder]
    [platform.utils.tagger :as tagger]
    [ring.util.response :as resp])
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
  (resp/redirect "/status"))


(defn pull-repository-info-all-handler
  [req]
  (async/thread
    (log/info "Getting a list of GitHub URLs...")
    (let [{{:keys [datasource]} :db} req
          project-urls (seeder/get-repository-urls datasource)
          project-tags (tagger/read-clojure-toolbox-data)]
      (log/info (count project-urls) "GitHub URLs will be fetched")
      (doall (map (fn [url]
                    (pull-repository-info datasource url project-tags)
                    (Thread/sleep 10000))
                  project-urls))))
  (resp/redirect "/status"))


(defn get-repository-info-all-handler
  [req]
  (let [{{:keys [datasource]} :db} req]
    {:status 200
     :headers {"Content-Type" "application/edn"}
     :body (pr-str (github-sql/select-repositories datasource))}))
