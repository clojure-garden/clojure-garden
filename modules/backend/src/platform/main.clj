(ns platform.main
  (:gen-class)
  (:require
    [aero.core :as aero]
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.tools.logging :as log]
    [integrant.core :as ig]))


(def default-modules
  [:platform.system/jetty
   :platform.system/database-migrator
   :platform.system/github-client])


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
  ([modules custom-profile]
   (let [profile (or custom-profile (some-> (System/getenv "RUN_PROFILE") keyword) :stage)
         config (read-config "config/config.edn" profile)]
     (-> config
         (assoc-in [:platform.system/app-runner :profile] profile)
         (ig/load-namespaces modules))
     config)))


(defn parse-keys
  "Parse config keys from a sequence of command line arguments."
  [args]
  (seq (filter keyword? (map edn/read-string args))))


(defn -main
  [& args]
  (try
    (let [modules (or (parse-keys args) default-modules)]
      (-> (load-system-config modules)
          (ig/prep modules)
          (ig/init modules)))
    (catch Exception ex (do (log/error (.getMessage ex))
                            (System/exit 1)))))
