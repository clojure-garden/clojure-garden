(ns platform.system.web.middleware.cors
  (:require
    [ring.middleware.cors :as cors]))


(def wrap-cors
  {:name        ::wrap-cors
   :description "CORS middleware."
   :wrap        (fn [handler {:keys [access-control-allow-origin
                                     access-control-allow-methods
                                     access-control-allow-headers]}]
                  (cors/wrap-cors
                    handler
                    :access-control-allow-origin (re-pattern access-control-allow-origin)
                    :access-control-allow-methods access-control-allow-methods
                    :access-control-allow-headers access-control-allow-headers))})
