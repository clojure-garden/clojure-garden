(ns platform.jdbc.wrappers
  (:require
    [next.jdbc :as jdbc]
    [next.jdbc.date-time]
    [next.jdbc.result-set :as rs]
    [platform.common :refer [safe]]))


(def default-opts
  {:builder-fn rs/as-unqualified-kebab-maps})


(defn execute!
  ([db query]
   (safe (jdbc/execute! db query default-opts)))
  ([db query opts]
   (safe (jdbc/execute! db query (merge default-opts opts)))))


(defn execute-one!
  ([db query]
   (safe (jdbc/execute-one! db query default-opts)))
  ([db query opts]
   (safe (jdbc/execute-one! db query (merge default-opts opts)))))
