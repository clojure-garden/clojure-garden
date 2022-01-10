(ns platform.router
  (:require
    [muuntaja.core :as m]
    [platform.api.v1.handlers :as handlers]
    [platform.system.web.middleware.context :as middleware.context]
    [platform.system.web.middleware.cors :as middleware.cors]
    [reitit.dev.pretty :as pretty]
    [reitit.middleware :as middleware]
    [reitit.ring :as ring]
    [reitit.ring.middleware.muuntaja :as middleware.muuntaja]
    [reitit.ring.middleware.parameters :as middleware.parameters]
    [ring.middleware.defaults :as middleware.defaults]
    [ring.middleware.keyword-params :as middleware.keyword-params]))


(def muuntaja-opts
  (assoc m/default-options :default-format "application/edn"))


(def default-opts
  {:exception pretty/exception
   :injection-router? false
   :injection-match? false
   :data {:muuntaja (m/create muuntaja-opts)}})


(defn with-develop-opts
  [opts]
  opts)


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
        ["" {:middleware [[:wrap-defaults defaults]
                          [:wrap-format]
                          [:wrap-parameters]
                          [:wrap-cors cors]]}
         ["/api"
          ["/v1"
           ["/github/repositories" {:middleware [[:wrap-context ctx]]
                                    :get {:handler handlers/get-repository-info-all-handler}}]]]
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
                         {:wrap-context        middleware.context/wrap-context
                          :wrap-cors           middleware.cors/wrap-cors
                          :wrap-defaults       middleware.defaults/wrap-defaults
                          :wrap-format         middleware.muuntaja/format-middleware
                          :wrap-keyword-params middleware.keyword-params/wrap-keyword-params
                          :wrap-parameters     middleware.parameters/parameters-middleware})))
      (ring/routes
        (ring/redirect-trailing-slash-handler {:method :strip})
        (ring/create-default-handler))
      {:injection-match? false
       :injection-router? false})))
