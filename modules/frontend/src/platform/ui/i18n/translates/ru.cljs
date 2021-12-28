(ns platform.ui.i18n.translates.ru
  "Russian translates"
  (:require
    ["antd/es/locale/ru_RU" :default antd-ru]
    ["dayjs/esm/locale/ru" :default dayjs-ru]))


(def dayjs-locale dayjs-ru)
(def antd-locale antd-ru)


(def dictionary
  {:greeting                  "Привет"
   :auth/unauthorized         "Доступ запрещен"
   :loading-page/spinner.text "Загрузка..."})
