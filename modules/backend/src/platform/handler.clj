(ns platform.handler
  (:require
    [platform.controllers.clojars-ctl :as clojars-ctl]
    [platform.controllers.github-ctl :as github-ctl]
    [platform.middleware.db-share :as db-share]
    [reitit.dev.pretty :as pretty]
    [reitit.ring :as ring]
    [reitit.ring.middleware.parameters :as parameters]
    [ring.middleware.keyword-params :as keyword-params]))


(defn config
  [db]
  {:conflicts nil
   :exception pretty/exception
   :data {:db db
          :middleware [parameters/parameters-middleware
                       keyword-params/wrap-keyword-params
                       db-share/db-middleware]}})


(defn run-app
  [db]
  (ring/ring-handler
    (ring/router
      [["/status" {:get (constantly {:status 200, :body "Ok"})}]
       ["/github/repository"
        ["/pull" {:get {:parameters {:query {:url string?}}
                        :handler github-ctl/pull-repository-info-handler}}]
        ["/pull-all" {:get {:handler github-ctl/pull-repository-info-all-handler}}]
        ["/all" {:get {:handler github-ctl/get-repository-info-all-handler}}]]
       ["/clojars/artifact" {:get {:parameters {:query {:url string?}}
                                   :handler clojars-ctl/pull-artifact-info-handler}}]
       ["/clojars/artifact/all" {:get {:handler clojars-ctl/pull-artifact-info-all-handler}}]]
      (config db))
    (ring/routes
      (ring/create-default-handler
        {:not-found (constantly {:status 404, :body "Not found"})}))))
