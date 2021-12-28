(ns platform.ui.pages.root
  (:require
    [antd.core :as antd]
    [clojure.string :as str]
    [platform.ui.components :as components]
    [platform.ui.pages.dashboard.view :as dashboard]
    [platform.ui.pages.landing.view :as landing]
    [platform.ui.pages.not-found.view :as not-found]
    [re-frame.core :as rf]))


(defn loading-page
  []
  [antd/layout {:class "loading-page"}
   [antd/layout-content {:class "page-content"}
    [components/spinner]]])


(defn page
  "Root page."
  []
  (if-not @(rf/subscribe [:app/initialized?])
    [loading-page]
    (let [route-name @(rf/subscribe [:navigation/route-name])]
      (condp str/starts-with? (str route-name)
        ":page/landing" [landing/page]
        ":page/dashboard" [dashboard/page]
        :else [not-found/page]))))
