(ns platform.system.jetty
  (:require
    [integrant.core :as ig]
    [ring.adapter.jetty :as jetty])
  (:import
    (org.eclipse.jetty.server
      Server)))


(defmethod ig/prep-key :platform.system/jetty [_ config]
  (merge {:port 8080} config))


(defmethod ig/init-key :platform.system/jetty [_ {:keys [router port] :as config}]
  (println "\nServer running on port: " port)
  (let [options (-> config (dissoc :router) (assoc :join? false))]
    (jetty/run-jetty router options)))


(defmethod ig/halt-key! :platform.system/jetty [_ ^Server server]
  (.stop server))
