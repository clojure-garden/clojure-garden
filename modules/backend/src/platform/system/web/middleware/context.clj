(ns platform.system.web.middleware.context)


(def wrap-context
  {:name        ::wrap-context
   :description "Context middleware."
   :wrap        (fn [handler ctx]
                  (fn
                    ([request]
                     (handler (assoc request :platform.system/ctx ctx)))
                    ([request respond raise]
                     (handler (assoc request :platform.system/ctx ctx) respond raise))))})
