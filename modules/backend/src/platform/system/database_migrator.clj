(ns platform.system.database-migrator
  (:require
    [integrant.core :as ig]
    [migratus.core :as migratus]))


(defmethod ig/init-key :platform.system/database-migrator [_ config]
  (migratus/migrate config))
