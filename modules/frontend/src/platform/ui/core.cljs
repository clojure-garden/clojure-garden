(ns platform.ui.core
  (:require
    [antd.core :as ant]
    [goog.dom :as gdom]
    [platform.ui.db :as db]
    [platform.ui.deps]
    [platform.ui.logger :as logger]
    [platform.ui.pages.root :as root]
    [platform.ui.router.core :as router]
    [re-frame.core :as rf]
    [reagent.dom :as dom]))


(defn setup-tools
  "Setup tools."
  []
  (logger/init!))


(defn app
  []
  (when-some [locale @(rf/subscribe [:i18n/locale])]
    [ant/config-provider {:locale        (:antd locale)
                          :componentSize "large"}
     [root/page]]))


(defn mount-root
  "Mount root component."
  {:dev/after-load true}
  []
  (when-some [root-elem (gdom/getElement "root")]
    (rf/clear-subscription-cache!)
    (router/init!)
    (dom/render [app] root-elem)))


(defn init!
  "UI initializer."
  {:export true}
  []
  (rf/dispatch-sync [::db/init])
  (setup-tools)
  (mount-root))
