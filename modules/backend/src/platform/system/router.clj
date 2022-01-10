(ns platform.system.router
  (:require
    [integrant.core :as ig]
    [platform.router :as router]))


(defmethod ig/init-key :platform.system/router [_ {:keys [profile cors db]}]
  (router/ring-handler {:profile profile
                        :cors   cors
                        :db     db}))
