(ns platform.ui.core
  (:require
    [goog.dom :as gdom]
    [platform.ui.db :as db]
    [platform.ui.logger :as logger]
    [re-frame.core :as rf]
    [reagent.dom :as dom]))


(defn square
  [x]
  (* x x))


(defn setup-tools
  "Setup tools."
  {:added "0.0.1"}
  []
  (logger/init!))


(defn app
  "App component."
  []
  [:h1 "Hello world!"])


(defn mount-root
  "Mount root component."
  {:dev/after-load true}
  []
  (when-let [root (gdom/getElement "root")]
    (rf/clear-subscription-cache!)
    (dom/render [app] root)))


(defn init!
  "UI initializer."
  {:export true}
  []
  (rf/dispatch-sync [::db/init])
  (setup-tools)
  (mount-root))
