(ns platform.ui.logger
  "UI logger."
  (:require
    [lambdaisland.glogi :as log]
    [lambdaisland.glogi.console :as console]
    [platform.ui.config :as config]))


(defn init!
  "Logger initializer."
  ([]
   (init! config/logger-level))

  ([logger-level]
   (let [level (or logger-level :off)]
     (console/install!)
     (log/set-levels
       {:glogi/root level
        'platform.ui      level})
     (log/debug :msg "Logger successfully initialized" :level level))))
