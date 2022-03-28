(ns platform.system.twitter-client
  (:require
    [integrant.core :as ig]
    [platform.twitter.api-client :as twitter]))


(defmethod ig/init-key :platform.system/twitter-client [_ options]
  (twitter/init options))
