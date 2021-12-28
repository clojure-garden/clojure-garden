(ns platform.ui.pages.landing.view
  (:require
    [antd.core :as antd]
    [platform.ui.components :as components]))


(defn content
  []
  [:div {:class "video"}
   [antd/typography-title {:class "text"
                           :strong true
                           :type "success"}
    "The navigator in the Clojure ecosystem"]
   [:video {:autoPlay true
            :muted    true
            :loop     true}
    [:source {:src  "ecosystem-fhd.mp4"
              :type "video/mp4"}]]])


(defn page
  []
  [antd/layout {:class "page landing-page"}
   [antd/layout-header {:class "page-header"}
    [components/header]]
   [antd/layout-content {:class "page-content"}
    [content]]
   [antd/layout-footer {:class "page-footer"}
    [components/footer]]])
