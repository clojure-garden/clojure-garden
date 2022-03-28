(ns platform.system.github-client
  (:require
    [clojure.tools.logging :as log]
    [integrant.core :as ig]
    [platform.github.api-client :as github]))


(defmethod ig/init-key :platform.system/github-client [_ options]
  (github/init options))


(defmethod ig/halt-key! :platform.system/github-client [_ service-name]
  (log/info "Shutting down service: " service-name)
  (github/shutdown))
