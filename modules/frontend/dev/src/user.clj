(ns user
  "Development helper functions."
  (:require
    [shadow.cljs.devtools.api :as shadow]))


(defn cljs-repl
  []
  (shadow/repl :app))
