(ns platform.twitter.api-client
  (:require
    [clojure.set :as set]
    [clojure.string :as str]
    [platform.common :refer [request-rest build-url transform-to-kebab-keywords
                             safe not-found-response-exception-handler]]))


(defonce ^:private settings (atom {}))


(defn- build-response-fields
  [fields]
  (->> fields
       (map name)
       (str/join ",")))


(defn- flatten-public-metrics
  [{:as obj :keys [public-metrics]}]
  (-> obj
      (merge public-metrics)
      (dissoc :public-metrics)))


(defn init
  "Initialize the module. This function must be called before calling
  all other public functions related to the Twitter API requests."
  [{:keys [service-name] :as config}]
  (swap! settings merge config)
  service-name)


(defn on-twitter-get-tweet-author
  [{{:keys [data]} :data errors :errors}]
  (prn data)
  (prn errors)
  (if-not errors
    (transform-to-kebab-keywords data)
    (throw (ex-info "Request to Twitter failed: can't fetch tweet author"
                    {:errors errors}))))


(defn on-twitter-search-recent-tweets
  [{:keys [data errors]}]
  (prn data)
  (prn errors)
  (if-not errors
    (-> data
        (transform-to-kebab-keywords)
        (set/rename-keys {:data :tweets}))
    (throw (ex-info "Request to Twitter failed: can't fetch recent tweets"
                    {:errors errors}))))



(defn get-tweet-author
  [author-id & {:as   _options
                :keys [user-fields]
                :or   {user-fields [:id :name :username]}}]
  (let [{:keys [rest-url oauth-token]} @settings
        url (build-url rest-url "users" author-id)
        options {:oauth-token oauth-token
                 :query-params {:user.fields (build-response-fields user-fields)}}]
    (safe
      (request-rest url on-twitter-get-tweet-author options)
      #(not-found-response-exception-handler % (format "Failed to fetch twitter user: %s!" author-id)))))


(defn update-author
  [{:as tweet :keys [author-id]}]
  (Thread/sleep 2500)
  (let [author (-> author-id
                   (get-tweet-author :user-fields [:id :name :username :created_at :description
                                                   :location :public_metrics :url :verified])
                   (flatten-public-metrics))]
    (-> tweet
        (assoc :author author)
        (dissoc :author-id))))


(defn search-recent-tweets
  "Searches for the recent tweets that match the query."
  [query & {:as   _options
            :keys [tweet-fields page-size]
            :or   {tweet-fields [:id :text] page-size 50}}]
  (let [{:keys [rest-url oauth-token]} @settings
        url (build-url rest-url "tweets/search/recent")
        options {:oauth-token oauth-token
                 :query-params {:query query
                                :tweet.fields (build-response-fields tweet-fields)
                                :max_results page-size}}]
    (safe
      (loop [{tweets :tweets {:keys [next-token]} :meta} (request-rest url on-twitter-search-recent-tweets options)
             acc []]
        (if (nil? next-token)
          (into [] (concat acc tweets))
          (do (Thread/sleep 2500)
              (recur (->> (assoc-in options [:query-params :pagination_token] next-token)
                          (request-rest url on-twitter-search-recent-tweets))
                     (into [] (concat acc tweets))))))
      #(not-found-response-exception-handler % "Failed to fetch recent tweets!"))))



(defn get-clojure-library-mentions
  [group-id artifact-id owner]
  (as-> (format "(%1$s OR %2$s OR #clojure OR #clj) (%3$s OR #%3$s) -is:retweet" group-id owner artifact-id) search
        (search-recent-tweets search :tweet-fields [:id :text :author_id :created_at :public_metrics] :page-size 50)
        (mapv (comp flatten-public-metrics update-author) search)))
