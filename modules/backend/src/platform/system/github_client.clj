(ns platform.system.github-client
  (:require
    [clojure.tools.logging :as log]
    [integrant.core :as ig]
    [platform.client.github :as github]))


(defmethod ig/init-key :platform.system/github-client [_ github-options]
  (github/init github-options))


(defmethod ig/halt-key! :platform.system/github-client [_ service-name]
  (log/info "Shutting down service: " service-name)
  (github/shutdown))
