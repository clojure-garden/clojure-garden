(ns platform.twitter.sql.tweet
  (:require
    [clojure.instant :as instant]
    [honey.sql.helpers :as helpers]
    [platform.storage.jdbc-wrappers :as jw])
  (:import
    (com.zaxxer.hikari
      HikariDataSource)
    (java.util
      UUID)))


(defn insert-author!
  [^HikariDataSource db {:as _tweet :keys [id name username location url description verified
                                           created-at followers-count following-count listed-count
                                           tweet-count]}]
  (jw/execute-one! db (-> (helpers/insert-into :tweet-user)
                          (helpers/values [{:id               (UUID/randomUUID)
                                            :id-str           id
                                            :name             name
                                            :screen-name      username
                                            :location         location
                                            :url              url
                                            :description      description
                                            :verified         verified
                                            :created-at       (some-> created-at
                                                                      instant/read-instant-date)
                                            :followers-count  followers-count
                                            :following-count  following-count
                                            :listed-count     listed-count
                                            :tweet-count      tweet-count}])
                          (helpers/on-conflict :screen-name)
                          (helpers/do-update-set :screen-name)
                          (helpers/returning :id)
                          jw/sql-format)))


(defn insert-tweet!
  [^HikariDataSource db {:as _tweet :keys [text created-at author-id
                                           retweet-count reply-count like-count quote-count]}]
  (jw/execute-one! db (-> (helpers/insert-into :tweet)
                          (helpers/values [{:id            (UUID/randomUUID)
                                            :content       text
                                            :created-at    (some-> created-at
                                                                   instant/read-instant-date)
                                            :author-id     author-id
                                            :retweet-count retweet-count
                                            :reply-count   reply-count
                                            :like-count    like-count
                                            :quote-count   quote-count}])
                          (helpers/on-conflict (helpers/on-constraint :unique_tweet_content_created_at))
                          (helpers/do-update-set :content)
                          (helpers/returning :id)
                          jw/sql-format)))


(defn insert-library-tweet!
  [^HikariDataSource db library-id tweet-id]
  (jw/execute! db (-> (helpers/insert-into :library-tweet)
                      (helpers/values [{:library-id library-id
                                        :tweet-id   tweet-id}])
                      (helpers/on-conflict :library-id :tweet-id)
                      (helpers/do-nothing)
                      jw/sql-format)))


(defn insert-tweets!
  [^HikariDataSource db library-id tweets]
  (when-not (empty? tweets)
    (into []
          (map (fn [{:as tweet :keys [author]}]
                 (let [{author-id :id} (insert-author! db author)
                       {tweet-id :id} (->> (assoc tweet :author-id author-id)
                                           (insert-tweet! db))]
                   (insert-library-tweet! db library-id tweet-id))))
          tweets)))
