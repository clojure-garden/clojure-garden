(ns platform.ui.router.core
  (:require
    [lambdaisland.glogi :as log]
    [platform.ui.router.events]
    [platform.ui.router.subs]
    [re-frame.core :as rf]
    [reitit.coercion.malli :as rcm]
    [reitit.frontend :as rfr]
    [reitit.frontend.easy :as rfe]))


(def routes
  [""
   ["/" {:name :page/landing, :private false}]
   ["/dashboard"
    ["" {:name :page/dashboard, :private false}]]])


(def router
  (rfr/router
    routes
    {:data {:coercion rcm/coercion, :private true}}))


(defn on-navigate
  "Router `on-navigate` entry point. This function to be called when route changes."
  [matched-route]
  (rf/dispatch [:navigation/set-route matched-route]))


(defn init!
  "Router initializer."
  []
  (rfe/start!
    router
    on-navigate
    ;; TODO: [2021-12-19, ilshat@sultanov.team] Use-fragments on the development and disable on the production mode?
    {:use-fragment true})
  (rf/dispatch-sync [:navigation/set-router router])
  (log/info :msg "Router successfully initialized"))
