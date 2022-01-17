(ns platform.ui.api
  (:require
    [clojure.set :as set]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [lambdaisland.fetch :as fetch]
    [lambdaisland.fetch.edn]
    [lambdaisland.glogi :as log]
    [platform.ui.utils.string :refer [format]]
    [re-frame.core :as rf]))


(defn fetch-repositories
  [{:keys [url on-success on-failure]}]
  (-> (fetch/get
        (format "%s/github/repositories" url)
        {:accept :edn})
      (.then
        (fn [res]
          (let [repositories (->> res :body :repositories)]
            (log/trace "Fetch GitHub repositories completed successfully" (count repositories))
            (->> repositories
                 (conj on-success)
                 (rf/dispatch)))))
      (.catch
        (fn [error]
          (log/error "Fetch GitHub repositories failed" error)
          (->> error
               (conj on-failure)
               (rf/dispatch))))))


(rf/reg-fx
  :api/fetch-github-repositories
  (fn [opts]
    (fetch-repositories opts)))


(rf/reg-event-db
  :api/fetch-github-repositories->success
  (fn-traced [db [_ repositories]]
    (->> repositories
         (sort-by :stargazer-count >)
         (assoc-in db [:data :github :repositories]))))


(rf/reg-event-db
  :api/fetch-github-repositories->failure
  (fn-traced [db [_ _error]]
    ;; TODO: [2022-01-12, ilshat@sultanov.team] show error notification?
    (assoc-in db [:data :github :repositories] [])))


(rf/reg-event-fx
  :api/fetch-github-repositories
  (fn-traced [{{{:keys [backend-api-url]} :api} :db} _]
    {:api/fetch-github-repositories
     {:url        backend-api-url
      :on-success [:api/fetch-github-repositories->success]
      :on-failure [:api/fetch-github-repositories->failure]}}))


(rf/reg-sub
  :github/repositories
  (fn [db]
    (get-in db [:data :github :repositories] [])))


(rf/reg-sub
  :github/repositories-tags
  :<- [:github/repositories]
  (fn [repositories]
    (->> repositories
         (reduce
           (fn [acc {:keys [topics]}]
             (into acc topics))
           #{})
         (sort))))


(rf/reg-sub
  :github/repositories-by-tags
  :<- [:github/repositories]
  :<- [:github/selected-tags]
  (fn [[repositories selected-tags]]
    (->> repositories
         (reduce
           (fn [acc {:as repo :keys [topics]}]
             (let [tags (set topics)]
               (if (seq (set/intersection selected-tags tags))
                 (conj acc repo)
                 acc)))
           []))))


(rf/reg-sub
  :github/selected-tags
  (fn [db]
    (get-in db [:dashboard :github :selected-tags] #{})))


(rf/reg-event-db
  :github/select-tag
  (fn [db [_ tags]]
    (assoc-in db [:dashboard :github :selected-tags] (set tags))))
