(ns platform.slack.api-client
  (:require
    [platform.common :refer [request-rest build-url transform-to-kebab-keywords
                             safe not-found-response-exception-handler]]))


(defonce ^:private settings (atom {}))


(defn init
  "Initialize the module. This function must be called before calling
  all other public functions related to the Slack API requests."
  [{:keys [service-name] :as config}]
  (swap! settings merge config)
  service-name)


(defn on-slack-search-messages-handler
  [{:keys [data errors]}]
  (if-not errors
    data
    (throw (ex-info "Request to Slack failed: can't fetch messages"
                    {:errors errors}))))


(defn search-messages
  "Searches for messages matching a query."
  [query]
  (let [{:keys [rest-url oauth-token-user]} @settings
        url (build-url rest-url "search.messages")
        options {:oauth-token oauth-token-user
                 :query-params {:query query
                                :count 2
                                :cursor "*"}}]
    (safe
      (loop [{{matches :matches {:keys [next-cursor]} :paging} :messages} (request-rest url on-slack-search-messages-handler options)
             acc []]
        (if (nil? next-cursor)
          (into [] (concat acc matches))
          (recur (->> (assoc-in options [:query-params :cursor] next-cursor)
                      (request-rest url on-slack-search-messages-handler))
                 (into [] (concat acc matches)))))
      #(not-found-response-exception-handler % "Failed to search for Slack messages!"))))


(defn search-archived-messages
  [query]
  (let [{:keys [zulip-rest-url oauth-token-zulip-user]} @settings
        url (build-url rest-url "search.messages")
        options {:oauth-token oauth-token-user
                 :query-params {:query query
                                :count 2
                                :cursor "*"}}]
    (safe
      (loop [{{matches :matches {:keys [next-cursor]} :paging} :messages} (request-rest url on-slack-search-messages-handler options)
             acc []]
        (if (nil? next-cursor)
          (into [] (concat acc matches))
          (recur (->> (assoc-in options [:query-params :cursor] next-cursor)
                      (request-rest url on-slack-search-messages-handler))
                 (into [] (concat acc matches)))))
      #(not-found-response-exception-handler % "Failed to search for archived messages!"))))


