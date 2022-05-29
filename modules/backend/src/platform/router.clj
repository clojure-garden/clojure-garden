(ns platform.router
  (:require
    [muuntaja.core :as m]
    [platform.api.v1.handlers :as handlers]
    [platform.clojars.clojars-ctl :as dev-clojars-handlers]
    [platform.github.github-ctl :as dev-github-handlers]
    [platform.system.web.middleware.context :as middleware.context]
    [platform.system.web.middleware.cors :as middleware.cors]
    [platform.twitter.twitter-ctl :as dev-twitter-handlers]
    [reitit.coercion.malli :as coercion.malli]
    [reitit.dev.pretty :as pretty]
    [reitit.middleware :as middleware]
    [reitit.ring :as ring]
    [reitit.ring.coercion :as ring.coercion]
    [reitit.ring.middleware.exception :as middleware.exception]
    [reitit.ring.middleware.muuntaja :as middleware.muuntaja]
    [reitit.ring.middleware.parameters :as middleware.parameters]
    [ring.middleware.defaults :as middleware.defaults]))


(def muuntaja-opts
  (assoc m/default-options :default-format "application/edn"))


(def default-opts
  {:exception         pretty/exception
   :injection-router? false
   :injection-match?  false
   :data              {:muuntaja (m/create muuntaja-opts)}})


(defn with-develop-opts
  [opts]
  opts)


;; https://github.com/metosin/reitit/issues/249
(defn exception-handler
  [exception _ex _request]
  {:status 500
   :body (str "{\"type\": \"exception\", \"class\": \"" (.getName (.getClass exception)) "\"}")})


(def exception-middleware
  (middleware.exception/create-exception-middleware
    (merge
      middleware.exception/default-handlers
      {::middleware.exception/wrap exception-handler})))


(defn ring-handler
  "Returns a reitit router."
  [{:keys [profile cors db]}]
  (let [develop? (= :dev profile)
        ctx {:db db}
        defaults (-> middleware.defaults/site-defaults
                     (assoc-in [:security :anti-forgery] false)
                     (assoc :session false))]
    (ring/ring-handler
      (ring/router
        ["" {:middleware [;; ring defaults
                          [:wrap-defaults defaults]
                          ;; query-params & form-params
                          [:wrap-parameters]
                          ;; encoding response body
                          [:wrap-format-response]
                          ;; exception handling
                          [:wrap-exception]
                          ;; decoding request body
                          [:wrap-format-request]
                          ;; coercing response body
                          ;; [:wrap-coerce-response]
                          ;; coercing request parameters
                          [:wrap-coerce-request]
                          ;; cross-origin resource sharing headers
                          [:wrap-cors cors]]}
         ["/api"
          ["/v1" {:middleware [[:wrap-context ctx]]}
           ["/github/repositories" {:get {:coercion coercion.malli/coercion
                                          :parameters {:query [:map
                                                               [:per-page  {:optional true} pos-int?]
                                                               [:page      {:optional true} pos-int?]
                                                               [:with-meta {:optional true} boolean?]
                                                               [:is-fork   {:optional true} boolean?]
                                                               ;; https://github.com/metosin/reitit/issues/298
                                                               ;; [:topics    {:optional true} vector?]
                                                               ]}
                                          :handler handlers/get-repositories-handler}}]
           ["/github/topics"       {:get {:coercion coercion.malli/coercion
                                          :parameters {:query [:map
                                                               [:per-page  {:optional true} pos-int?]
                                                               [:page      {:optional true} pos-int?]
                                                               [:with-meta {:optional true} boolean?]]}
                                          :handler handlers/get-topics-handler}}]]]
         ["/dev"
          ["/pull" {:middleware [[:wrap-context ctx]]}
           ["/github"  {:get {:handler dev-github-handlers/pull-repository-info-all-handler}}]
           ["/clojars" {:get {:handler dev-clojars-handlers/pull-artifact-info-all-handler}}]
           ["/twitter" {:get {:handler dev-twitter-handlers/pull-tweets-all-handler}}]]]
         ["/health"
          ["/alive" {:get (constantly {:status 200})}]
          ["/ready" {:get (constantly {:status 200})}]]
         ["/ping" {:get {:handler (constantly {:status 200
                                               :headers {"Content-Type" "text/plain"}
                                               :body     "pong"})}}]
         ["/echo" {:get {:handler (fn [req]
                                    {:status 200
                                     :body   req})}}]]
        (cond-> default-opts
          develop? with-develop-opts
          :always (assoc ::middleware/registry
                         {:wrap-context          middleware.context/wrap-context
                          :wrap-cors             middleware.cors/wrap-cors
                          :wrap-coerce-response  ring.coercion/coerce-response-middleware
                          :wrap-coerce-request   ring.coercion/coerce-request-middleware
                          :wrap-coerce-exception ring.coercion/coerce-exceptions-middleware
                          :wrap-defaults         middleware.defaults/wrap-defaults
                          :wrap-exception        exception-middleware
                          :wrap-format-response  middleware.muuntaja/format-response-middleware
                          :wrap-format-request   middleware.muuntaja/format-request-middleware
                          :wrap-parameters       middleware.parameters/parameters-middleware})))
      (ring/routes
        (ring/redirect-trailing-slash-handler {:method :strip})
        (ring/create-default-handler))
      {:injection-match? false
       :injection-router? false})))
