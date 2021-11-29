(ns platform.middleware.db-share)


(def db-middleware
  {:name ::db
   :compile (fn [{:keys [db] :as opts} _]
              (fn [handler]
                (fn [req]
                  (handler (assoc req :db (first db))))))})

