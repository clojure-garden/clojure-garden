(ns platform.ui.pages.not-found.view
  (:require
    [antd.core :as antd]
    [platform.ui.components :as components]
    [platform.ui.utils.string :refer [format]]))


(defn content
  []
  [:h1 "Not found page"])


(defn page
  []
  [antd/layout {:class "page"}
   [antd/layout-header {:class "page-header"}
    [components/header]]
   [antd/layout-content {:class "page-content"}
    [content]]
   [antd/layout-footer {:class "page-footer"}
    [components/footer]]])
