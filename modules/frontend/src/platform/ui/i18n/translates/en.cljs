(ns platform.ui.i18n.translates.en
  "English translates."
  (:require
    ["antd/es/locale/en_US" :default antd-en]
    ["dayjs/esm/locale/en" :default dayjs-en]))


(def dayjs-locale dayjs-en)
(def antd-locale antd-en)


(def dictionary
  {:greeting                  "Hello"
   :auth/unauthorized         "Access denied"
   :loading-page/spinner.text "Loading..."})
