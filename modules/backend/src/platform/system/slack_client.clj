(ns platform.system.slack-client
  (:require
    [integrant.core :as ig]
    [platform.slack.api-client :as slack]))


(defmethod ig/init-key :platform.system/slack-client [_ options]
  (slack/init options))
