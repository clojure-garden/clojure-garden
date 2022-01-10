(ns platform.main
  (:gen-class)
  (:require
    [platform.system :as system]
    [platform.system.hooks :as hooks]))


(defn -main
  [& [profile]]
  (hooks/set-global-exception-hook!)
  (let [profile (or profile
                    (some-> (System/getenv "RUN_PROFILE") keyword)
                    :stage)
        system (system/start! profile)]
    (hooks/set-os-signals-hook!
      #(system/stop! system))))
