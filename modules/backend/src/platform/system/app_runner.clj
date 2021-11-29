(ns platform.system.app-runner
  (:require
    [integrant.core :as ig]
    [platform.handler :as handler]))


(defmethod ig/init-key :platform.system/app-runner [_ {:keys [db]}]
  (handler/run-app [db]))
