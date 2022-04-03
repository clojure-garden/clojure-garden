(ns platform.ui.pages.dashboard.view
  (:require
    [antd.core :as antd]
    [antd.icons :as icons]
    [clojure.string :as str]
    [platform.ui.components :as components]
    [platform.ui.utils.string :refer [format date-to-string]]
    [re-frame.core :as rf]
    [reagent.core :as r]))


(defn repositories-list
  [repositories {:as _pagination :keys [total-count]}]
  (-> [:div {:style {:margin-top "20px"}}
       (into [antd/row {:gutter [12 12]}]
             (for [repos (partition-all 4 repositories)]

               (for [{:keys [owner name description license-name license-url
                             stargazer-count contributor-count fork-count topics
                             total-downloads updated-at]} repos]
                 (let [title (format "%s/%s \uD83C\uDF31" owner name)
                       description (-> (or description "No description")
                                       (str/capitalize)
                                       (str/replace #"(?:\.|!|\?)$" ""))
                       license-name (or license-name "Unknown License")
                       updated-at (date-to-string updated-at)
                       github-link (format "https://github.com/%s/%s" owner name)]
                   [antd/col {:span 6 :class-name "gutter-row"}
                    [antd/card {:title title
                                :extra (r/as-element [antd/typography-link {:href github-link
                                                                            :target "_blank"} "More"])
                                :style {:height "100%"}}
                     [antd/space {:direction "vertical" :align "baseline"}
                      [antd/typography-paragraph {:type "secondary"
                                                  :ellipsis {:expandable false
                                                             :rows 2}} description]
                      [antd/space {:direction "horizontal"}
                       (when-not (some #{"clj" "clojure" "cljs" "clojurescript"} topics)
                         [antd/tag {:color "yellow"} "not specified"])
                       (when (some #{"clj" "clojure"} topics)
                         [antd/tag {:color "blue"} "clj"])
                       (when (some #{"cljs" "clojurescript"} topics)
                         [antd/tag {:color "red"} "cljs"])]
                      [antd/space {:direction "horizontal"}
                       [icons/star-outlined]
                       [antd/typography-text stargazer-count]
                       [icons/team-outlined]
                       [antd/typography-text contributor-count]
                       [icons/fork-outlined]
                       [antd/typography-text fork-count]
                       [icons/download-outlined]
                       [antd/typography-text total-downloads]
                       [icons/format-painter-outlined]
                       [antd/typography-text updated-at]]
                      [antd/typography-link {:href license-url :target "_blank"} license-name]]]]))))]
      (conj [:div {:style {:margin-top "20px"}}
             [antd/pagination {:defaultCurrent 1
                               :defaultPageSize 20
                               :onChange #(rf/dispatch [:github/fetch-repositories-by-tag {:page %1 :page-size %2}])
                               :total (or total-count 0)}]])))


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
  (let [tags                    @(rf/subscribe [:github/topics])
        selected-tags           @(rf/subscribe [:github/selected-tags])
        repositories            @(rf/subscribe [:github/repositories])
        pagination              @(rf/subscribe [:github/pagination])]
    [antd/layout-content {:style {:margin-top "20px"}}
     [antd/row
      [antd/col {:span   20
                 :offset 2}
       [select-tags tags selected-tags]
       [repositories-list repositories pagination]]]]))


(defn page
  []
  [antd/layout {:class "page dashboard-page"}
   [antd/layout-header {:class "page-header"}
    [components/header]]
   [antd/layout-content {:class "page-content"}
    [content]]
   [antd/layout-footer {:class "page-footer"}
    [components/footer]]])
