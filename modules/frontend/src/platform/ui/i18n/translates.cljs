(ns platform.ui.i18n.translates
  "Multi-Language supports (i18n)."
  (:require
    [platform.ui.config :as config]
    [platform.ui.i18n.translates.en :as en]
    [platform.ui.i18n.translates.ru :as ru]
    [tongue.core :as tongue]))


(def fallback
  (keyword config/default-language))


(def locales
  {:ru {:dayjs ru/dayjs-locale
        :antd  ru/antd-locale}
   :en {:dayjs en/dayjs-locale
        :antd  en/antd-locale}})


(def dictionaries
  {:ru              ru/dictionary
   :en              en/dictionary
   :tongue/fallback fallback})


(defn translator
  [language]
  (let [translate-fn (tongue/build-translate dictionaries)]
    (fn translate
      [key params]
      (if (seq params)
        (apply translate-fn language key params)
        (translate-fn language key)))))


(defn get-locale
  [language]
  (or (get locales language)
      (get locales fallback)))
