(ns platform.system.jetty
  (:require
    [integrant.core :as ig]
    [ring.adapter.jetty :as jetty]))


(defmethod ig/prep-key :platform.system/jetty [_ config]
  (merge {:port 8080} config))


(defmethod ig/init-key :platform.system/jetty [_ {:keys [handler port] :as config}]
  (println "\nServer running on port: " port)
  (let [options (-> config (dissoc :handler) (assoc :join? false))]
    (jetty/run-jetty handler options)))


(defmethod ig/halt-key! :platform.system/jetty [_ server]
  (.stop server))
