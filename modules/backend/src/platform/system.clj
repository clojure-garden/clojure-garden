(ns platform.system
  (:require
    [aero.core :as aero]
    [clojure.java.io :as io]
    [integrant.core :as ig]))


(def default-modules
  [::jetty
   ::database-migrator
   ::clojars-client
   ::github-client
   ::slack-client
   ::twitter-client])


(defmethod aero/reader 'ig/ref
  [_ _ value]
  (ig/ref value))


(defn read-config
  "Read system configuration profile."
  [path profile]
  (if path
    (aero/read-config (io/resource path) {:profile profile})
    (throw (ex-info (str "Resource not found: " path)
                    {:path path}))))


(defn load-system-config
  "Read configuration for specified modules and load each namespace referenced
   by a top-level key in the config file. If the custom-profile is nil, then
   the RUN_PROFILE environment variable value will be used, otherwise the default
   profile (:stage) will be loaded."
  ([modules]
   (load-system-config modules nil))
  ([modules profile]
   (let [config (read-config "config/config.edn" profile)]
     (-> config
         (assoc-in [:platform.system/router :profile] profile)
         (ig/load-namespaces modules))
     config)))


(defn start!
  [profile]
  (-> (load-system-config default-modules profile)
      (ig/prep default-modules)
      (ig/init default-modules)))


(defn stop!
  [system]
  (ig/halt! system))
