(ns platform.middleware.db-share)


(def db-middleware
  {:name ::db
   :compile (fn [{:keys [db] :as _opts} _]
              (fn [handler]
                (fn [req]
                  (handler (assoc req :db (first db))))))})

