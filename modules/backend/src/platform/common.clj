(ns platform.common
  (:require
    [camel-snake-kebab.core :as csk]
    [camel-snake-kebab.extras :as cske]
    [cheshire.core :as json]
    [clojure.core.async :as async]
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    [hato.client :as http]
    [re-graph.core :as re-graph]))


(defmacro safe
  "Extend try-catch.
   Usage:
   * (safe (/ 1 0)) ;;=> nil
   * (safe (/ 1 0) #(ex-message %)) ;;=> \"Divide by zero\""
  ([body]
   `(try
      ~body
      (catch Exception  ~'_)))
  ([body with]
   `(try
      ~body
      (catch Exception ex#
        (~with ex#)))))


(defn success-codes?
  [code]
  (contains? #{200 201 202 204} code))


(defn parse-int
  [number-string]
  (safe
    (Integer/parseInt number-string)))


(defn get-percentage
  [place total-count]
  (/ (* place 100.0) total-count))


(defn load-query
  [queries-map query-name]
  (get-in queries-map [:query query-name]))


(defn build-url
  [url-base & paths]
  (let [url-base (str/replace url-base #"/$" "")]
    (str url-base "/"
         (str/join "/" paths))))


(defn transform-to-kebab-keywords
  [coll]
  (cske/transform-keys csk/->kebab-case-keyword coll))


(defn transform-to-camel-strings
  [coll]
  (cske/transform-keys csk/->camelCaseString coll))


(defn make-graphql-query
  [{:keys [query variables]} service-key handler]
  (re-graph/query service-key query variables handler))


(defn request-graphql
  [query-map service-key handler]
  (let [out (async/chan)]
    (make-graphql-query query-map service-key
                        (fn [payload]
                          (async/put! out payload)
                          (async/close! out)))
    (handler (async/<!! out))))


(defn request-rest
  ([url handler]
   (request-rest url handler {}))
  ([url handler opts]
   (let [{response-status :status :as response} (->> opts
                                                     (merge {:http-client {:redirect-policy :normal}
                                                             :throw-exceptions false})
                                                     (http/get url))
         headers (:headers response)
         body (json/parse-string-strict (:body response) csk/->kebab-case-keyword)]
     (if (success-codes? response-status)
       (handler {:headers headers :data body})
       (handler {:headers headers :errors (assoc body :response-status response-status)})))))


(defn not-found-response-exception-handler
  [ex message]
  (let [{{:keys [response-status]} :errors} (ex-data ex)]
    (if (= 404 response-status)
      (do
        (log/warn message)
        nil)
      (throw ex))))
