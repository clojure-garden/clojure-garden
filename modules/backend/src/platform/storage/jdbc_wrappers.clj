(ns platform.storage.jdbc-wrappers
  (:require
    [clojure.tools.logging :as log]
    [honey.sql :as sql]
    [next.jdbc :as jdbc]
    [next.jdbc.date-time]
    [next.jdbc.result-set :as rs])
  (:import
    (org.postgresql.util
      PSQLException)))


(def default-opts
  {:builder-fn rs/as-unqualified-kebab-maps})


(defn sql-format
  ([query]
   (sql/format query))
  ([query opts]
   (sql/format query opts)))


(defmacro safe
  [body]
  `(try
     ~body
     (catch PSQLException ex#
       (let [message#   (ex-message ex#)
             cause#     (.getCause ex#)
             sql-state# (.getSQLState ex#)]
         (log/trace ex# message# {:type      (.getCanonicalName (type ex#))
                                  :cause     cause#
                                  :sql-state sql-state#})))
     (catch Exception ex#
       (let [message# (ex-message ex#)
             cause#   (.getCause ex#)]
         (log/trace ex# message# {:type    (.getCanonicalName (type ex#))
                                  :cause   cause#})))))


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


(defmacro with-transaction
  [[sym transactable opts] & body]
  `(jdbc/with-transaction [~sym ~transactable ~opts] ~@body))
