(ns platform.ui.pages.dashboard.view
  (:require
    [antd.core :as antd]
    [platform.ui.components :as components]
    [platform.ui.utils.string :refer [format]]
    [re-frame.core :as rf]))


(defn repositories-list
  [repositories]
  (into [:div {:style {:margin-top "20px"}}]
        (for [repos (partition-all 4 repositories)]
          (into [antd/row {:gutter 2}]
                (for [{:keys [name owner]} repos]
                  (let [description (format "%s/%s" owner name)]
                    [antd/col {:span 6}
                     [antd/card
                      [antd/card-meta
                       {:description description}]]]))))))


(defn select-tags
  [tags selected-tags]
  (into [antd/select {:mode        :multiple
                      :placeholder "Select tags"
                      :onChange    #(rf/dispatch [:github/select-tag %])
                      :style       {:width "100%"}
                      :value       selected-tags}]
        (for [tag tags
              :when (not (contains? selected-tags tag))]
          [antd/select-option {:key tag} tag])))


(defn content
  []
  (let [tags          @(rf/subscribe [:github/repositories-tags])
        selected-tags @(rf/subscribe [:github/selected-tags])
        repositories  @(rf/subscribe [:github/repositories-by-tags])]
    [antd/layout-content {:style {:margin-top "20px"}}
     [antd/row
      [antd/col {:span   20
                 :offset 2}
       [select-tags tags selected-tags]
       [repositories-list repositories]]]]))


(defn page
  []
  [antd/layout {:class "page dashboard-page"}
   [antd/layout-header {:class "page-header"}
    [components/header]]
   [antd/layout-content {:class "page-content"}
    [content]]
   [antd/layout-footer {:class "page-footer"}
    [components/footer]]])
