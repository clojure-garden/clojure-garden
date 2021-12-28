(ns platform.ui.pages.dashboard.view
  (:require
    [antd.core :as antd]
    [platform.ui.components :as components]
    [platform.ui.utils.string :refer [format]]))


(defn content
  []
  [antd/page-header
   {:title "Dashboard page"
    :onBack (constantly nil)}])


(defn page
  []
  [antd/layout {:class "page dashboard-page"}
   [antd/layout-header {:class "page-header"}
    [components/header]]
   [antd/layout-content {:class "page-content"}
    [content]]
   [antd/layout-footer {:class "page-footer"}
    [components/footer]]])
