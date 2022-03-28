(ns platform.slack.slack-message
  (:require
    [clojure.instant :as instant]
    [honey.sql.helpers :as helpers]
    [platform.storage.jdbc-wrappers :as jw])
  (:import
    (com.zaxxer.hikari
      HikariDataSource)
    (java.util
      UUID)))


(defn insert-message!
  [^HikariDataSource db {:as _message :keys [content content_type timestamp sender_id stream_id]}]
  (jw/execute-one! db (-> (helpers/insert-into :slack-messages)
                          (helpers/values [{:id            (UUID/randomUUID)
                                            :content       content
                                            :created-at    timestamp
                                            :author-id     sender_id
                                            :channel-id    stream_id}])
                          (helpers/on-conflict (helpers/on-constraint :content))
                          (helpers/do-update-set :content)
                          (helpers/returning :id)
                          jw/sql-format)))


(defn insert-library-slack-message!
  [^HikariDataSource db library-id message-id]
  (jw/execute! db (-> (helpers/insert-into :library-slack-message)
                      (helpers/values [{:library-id library-id
                                        :message-id message-id}])
                      (helpers/on-conflict :library-id :message-id)
                      (helpers/do-nothing)
                      jw/sql-format)))


(defn insert-messages!
  [^HikariDataSource db library-id messages]
  (when-not (empty? messages)
    (into []
          (map (fn [message]
                 (let [{message-id :id} (->> (insert-message! db message))]
                   (insert-library-slack-message! db library-id message-id))))
          messages)))
