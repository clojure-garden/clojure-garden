(ns platform.system.database
  (:require
    [hikari-cp.core :as hk]
    [integrant.core :as ig]))


(defmethod ig/init-key :platform.system/database [_ datasource-options]
  {:datasource (hk/make-datasource datasource-options)})


(defmethod ig/halt-key! :platform.system/database [_ db-spec]
  (-> db-spec :datasource hk/close-datasource))
