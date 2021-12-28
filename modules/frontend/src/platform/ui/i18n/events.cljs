(ns platform.ui.i18n.events
  (:require
    ["dayjs/esm" :default dayjs]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [platform.ui.i18n.translates :as translates]
    [re-frame.core :as rf]))


(rf/reg-fx
  :dayjs/set-locale
  (fn [language]
    (.locale dayjs language)))


(rf/reg-event-fx
  :dayjs/set-locale
  (fn [_ [_ language]]
    {:dayjs/set-locale language}))


(rf/reg-event-fx
  :i18n/set-language
  (fn-traced [{db :db} [_ language]]
    (let [language' (if (keyword? language) language (keyword language))]
      {:db         (assoc-in db [:i18n :language] language')
       :dispatch-n [[:i18n/set-locale language']
                    [:i18n/set-translator language']]})))


(rf/reg-event-db
  :i18n/set-locale
  (fn-traced [db [_ language]]
    (let [locale (translates/get-locale language)]
      (assoc-in db [:i18n :locale] locale))))


(rf/reg-event-db
  :i18n/set-translator
  (fn-traced [db [_ language]]
    (let [translator (translates/translator language)]
      (-> db
          (assoc-in [:i18n :translator] translator)
          (assoc-in [:app :initialized?] true)))))
