(ns platform.ui.db
  "UI state."
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [platform.ui.config :as config]
    [platform.ui.i18n.deps]
    [platform.ui.i18n.translates :as translates]
    [re-frame.core :as rf]))


;;
;; Initial state
;;

(rf/reg-event-fx
  ::init
  (fn-traced [_ _]
    (let [db     {:app  {:initialized? false
                         :build-meta   {:version         config/version
                                        :build-number    config/build-number
                                        :build-timestamp config/build-timestamp
                                        :git-url         config/git-url
                                        :git-branch      config/git-branch
                                        :git-sha         config/git-sha}}
                  :api  {:backend-api-url config/backend-api-url}
                  :i18n {:languages (->> :tongue/fallback
                                         (dissoc translates/dictionaries)
                                         (keys)
                                         (set))}}
          events [[:i18n/set-language config/default-language]]]
      {:db         db
       :dispatch-n events})))


(rf/reg-sub
  :app/initialized?
  (fn [db]
    (-> db
        (get-in [:app :initialized?])
        (boolean))))



;;
;; Build meta
;;

(rf/reg-sub
  :app/build-meta
  (fn [db]
    (get-in db [:app :build-meta])))


(rf/reg-sub
  :app/version
  :<- [:app/build-meta]
  (fn [build-meta]
    (:version build-meta)))


(rf/reg-sub
  :app/build-number
  :<- [:app/build-meta]
  (fn [build-meta]
    (:build-number build-meta)))


(rf/reg-sub
  :app/build-timestamp
  :<- [:app/build-meta]
  (fn [build-meta]
    (:build-timestamp build-meta)))


(rf/reg-sub
  :app/git-url
  :<- [:app/build-meta]
  (fn [build-meta]
    (:git-url build-meta)))


(rf/reg-sub
  :app/git-branch
  :<- [:app/build-meta]
  (fn [build-meta]
    (:git-branch build-meta)))


(rf/reg-sub
  :app/git-sha
  :<- [:app/build-meta]
  (fn [build-meta]
    (:git-sha build-meta)))
