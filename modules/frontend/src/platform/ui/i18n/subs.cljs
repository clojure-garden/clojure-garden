(ns platform.ui.i18n.subs
  (:require
    [platform.ui.i18n.translates :as translates]
    [platform.ui.utils.string :refer [format]]
    [re-frame.core :as rf]))


(rf/reg-sub
  :i18n
  (fn [db _]
    (:i18n db)))


(rf/reg-sub
  :i18n/languages
  :<- [:i18n]
  (fn [i18n]
    (:languages i18n)))


(rf/reg-sub
  :i18n/language
  :<- [:i18n]
  (fn [i18n]
    (:language i18n)))


(rf/reg-sub
  :i18n/locale
  :<- [:i18n]
  (fn [i18n]
    (:locale i18n)))


(rf/reg-sub
  :i18n/translator
  :<- [:i18n]
  (fn [i18n]
    (:translator i18n)))



;;
;; Create i18n subscriptions from the dictionary
;;

;; TODO: [2021-12-19, ilshat@sultanov.team] Throw an error if there is a difference in keys?

(doseq [key (->> :tongue/fallback
                 (dissoc translates/dictionaries)
                 (vals)
                 (mapcat keys)
                 (set))]
  (when-some [sub-name (cond
                         (qualified-keyword? key) (keyword (format "i18n.%s/%s" (namespace key) (name key)))
                         (simple-keyword? key) (keyword (format "i18n/%s" (name key))))]
    (rf/reg-sub
      sub-name
      :<- [:i18n/translator]
      (fn [translator [_ params]]
        (try
          (translator key params)
          (catch :default _
            (js/console.error :msg "Unable to translate" :key key :params params)))))))
