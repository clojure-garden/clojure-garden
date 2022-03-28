(ns platform.twitter.twitter-ctl
  (:require
    [clojure.core.async :as async]
    [clojure.tools.logging :as log]
    [platform.clojars.clojars-sql :as clojars-sql]
    [platform.twitter.api-client :as twitter-client]
    [platform.twitter.sql.tweet :as tweet-sql])
  (:import
    (clojure.lang
      ExceptionInfo)))


(defn pull-tweets
  [datasource {:as _library :keys [id group-id artifact-id]}]
  (log/info "Pulling library tweets:" (str group-id "/" artifact-id))
  (let [tweet-count (->> (twitter-client/get-clojure-library-mentions group-id artifact-id group-id)
                         (tweet-sql/insert-tweets! datasource id))]
    (log/info (format "%d tweets pulled!" tweet-count))))


(defn pull-repository-info-all-handler
  [req]
  (async/thread
    (log/info "Getting a list of libraries...")
    (let [datasource (get-in req [:platform.system/ctx :db :datasource])
          libraries (clojars-sql/select-libraries datasource)]
      (log/info  "Fetching tweets for " (count libraries) " libraries")
      (into []
            (map #(pull-tweets datasource %))
            libraries)))
  {:status 202})
