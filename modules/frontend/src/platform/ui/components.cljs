(ns platform.ui.components
  (:require
    [antd.core :as antd]
    [antd.icons :as icons]
    [platform.ui.components.footer.subs :as subs]
    [re-frame.core :as rf]
    [reagent.core :as r]))


(defn logotype
  []
  [:div {:class "logotype"}
   [:a {:href @(rf/subscribe [:href :page/landing])}
    [:span {:class "logotype-image"}]
    [:span {:class "logotype-text"}
     "Clojure Garden"]]])


(defn user-menu
  []
  [antd/menu
   [antd/menu-item {:key 1}
    [icons/user-outlined]
    [:span "Profile"]]
   [antd/menu-item {:key 2}
    [icons/setting-outlined]
    [:span "Settings"]]
   [antd/menu-item {:key 3}
    [icons/logout-outlined]
    [:span "Sign out"]]])


(defn user
  []
  [:div {:class "user"}
   [antd/dropdown {:overlay          (r/as-element [user-menu])
                   :overlayClassName "user-menu"
                   :placement        "bottomRight"
                   :trigger          ["click"]}
    [antd/badge {:count 42}
     [antd/avatar {:class "avatar"}
      [icons/user-outlined]]]]])


(defn nav
  []
  [:div {:class "nav"}
   [antd/menu {:mode  "horizontal"
               :class "nav-links"}
    [antd/menu-item {:key 1} [:a {:href @(rf/subscribe [:href :page/dashboard])} "Dashboard"]]]])


(defn header
  []
  [:div {:class "header-menu"}
   [logotype]
   [nav]
   #_[user]])


(defn footer
  []
  (let [copyright @(rf/subscribe [::subs/copyright])
        {:keys [link label]} @(rf/subscribe [::subs/version])]
    [:div {:class "copyright"}
     [antd/space {:align     :central
                  :direction :horizontal
                  :size      :large
                  :split     (r/as-element [antd/divider {:type :vertical}])}
      [:span copyright]
      [:a {:target "_blank", :href link} label]]]))


(defn spinner
  []
  [antd/spin
   {:tip       @(rf/subscribe [:i18n.loading-page/spinner.text])
    :indicator (r/as-element [icons/loading-outlined {:font-size 48, :spin true}])}])
