(ns platform.system.clojars-client
  (:require
    [integrant.core :as ig]
    [platform.clojars.api-client :as clojars]))


(defmethod ig/init-key :platform.system/clojars-client [_ options]
  (clojars/init options))

