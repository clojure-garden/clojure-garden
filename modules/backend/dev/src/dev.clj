(ns dev
  (:require
    [clojure.pprint :as pprint]
    [clojure.repl :refer :all]
    [clojure.tools.namespace.repl :refer [refresh]]
    [integrant.core :as ig]
    [integrant.repl :as ig-repl]
    [integrant.repl.state :as ig-state]
    [platform.main :as system]))


(clojure.tools.namespace.repl/set-refresh-dirs "dev/src" "src" "test")


(defn integrant-prep!
  "Load Integrant configuration to be used to start the system.
   Available profile values: :dev, :stage, :test, :prod."
  [profile & {:keys [modules] :as opts}]
  (ig-repl/set-prep!
    (fn []
      (let [modules (or modules system/default-modules)]
        (-> (system/load-system-config modules profile)
            (ig/prep modules))))))


(defn go
  "Start the system services with Integrant REPL."
  ([] (go :dev))
  ([profile] (integrant-prep! profile) (ig-repl/go)))


(defn reset
  "Read updates from the configuration and restart the system services with Integrant REPL.
   Refreshes only changed source code files."
  ([] (reset :dev))
  ([profile] (integrant-prep! profile) (ig-repl/reset)))


(defn reset-all
  "Read updates from the configuration and restart the system services with Integrant REPL.
   Refreshes all source code files."
  ([] (reset-all :dev))
  ([profile] (integrant-prep! profile) (ig-repl/reset-all)))


(defn stop
  "Shutdown all services."
  []
  (ig-repl/halt))


(defn system
  "The running system configuration."
  []
  ig-state/system)


(defn config
  "The current system configuration."
  []
  ig-state/config)


(comment
  (go)
  (reset)
  (reset-all)
  (config)
  (system)
  (stop)
  (pprint/pprint ig-state/system))
