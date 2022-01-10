(ns platform.system.app-runner
  (:require
    [integrant.core :as ig]
    [platform.router :as handler]))


(defmethod ig/init-key :platform.system/app-runner [_ {:keys [profile cors db]}]
  (handler/ring-handler [{:profile profile
                          :cors cors
                          :db db}]))
