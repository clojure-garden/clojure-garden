(ns platform.system.hooks
  (:require
    [clojure.tools.logging :as log]))


(defn set-global-exception-hook!
  "Catch any uncaught exceptions and log them."
  []
  (Thread/setDefaultUncaughtExceptionHandler
    (reify Thread$UncaughtExceptionHandler
      (uncaughtException
        [_ thread ex]
        (log/error ex (.getMessage ex) {:thread (.getName thread)})))))


(defn set-os-signals-hook!
  "Catch SIGTERM, SIGINT and other OS signals.
   Executes given `f` function."
  [f]
  (.addShutdownHook
    (Runtime/getRuntime)
    (Thread.
      (fn []
        (log/trace "Got shutdown signal from OS")
        (f)
        (Thread/sleep 10000)
        (shutdown-agents)))))
