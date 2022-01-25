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
  [{:keys [url selected-tags page per-page on-success on-failure]}]
  (let [query-str (format "%s/github/repositories" url)]
    (-> (fetch/get
          query-str
          {:accept :edn
           :query-params {:page      page
                          :per-page  per-page
                          :topics    selected-tags
                          :with-meta true}})
        (.then
          (fn [res]
            (let [repositories (->> res :body :repositories)
                  meta         (->> res :body :_meta)]
              (log/trace "Fetch GitHub repositories completed successfully" (count repositories))
              (->> {:repositories repositories :meta meta}
                   (conj on-success)
                   (rf/dispatch)))))
        (.catch
          (fn [error]
            (log/error "Fetch GitHub repositories failed" error)
            (->> error
                 (conj on-failure)
                 (rf/dispatch)))))))


(defn fetch-topics
  [{:keys [url on-success on-failure]}]
  (-> (fetch/get
        (format "%s/github/topics" url)
        {:accept :edn
         :query-params {:per-page 5000
                        :page     1}})
      (.then
        (fn [res]
          (let [topics (->> res :body :topics)]
            (log/trace "Fetch GitHub topics completed successfully" (count topics))
            (->> topics
                 (conj on-success)
                 (rf/dispatch)))))
      (.catch
        (fn [error]
          (log/error "Fetch GitHub topics failed" error)
          (->> error
               (conj on-failure)
               (rf/dispatch))))))


(rf/reg-fx
  :api/fetch-github-repositories
  (fn [opts]
    (fetch-repositories opts)))


(rf/reg-fx
  :api/fetch-github-topics
  (fn [opts]
    (fetch-topics opts)))


(rf/reg-event-db
  :api/fetch-github-repositories->success
  (fn-traced [db [_ {:keys [repositories meta]}]]
    (let [db (assoc-in db [:dashboard :github :pagination] meta)]
      (->> repositories
           (sort-by :stargazer-count >)
           (assoc-in db [:data :github :repositories])))))


(rf/reg-event-db
  :api/fetch-github-repositories->failure
  (fn-traced [db [_ _error]]
    ;; TODO: [2022-01-12, ilshat@sultanov.team] show error notification?
    (assoc-in db [:data :github :repositories] [])))


(rf/reg-event-db
  :api/fetch-github-topics->success
  (fn-traced [db [_ topics]]
    (->> topics
         (sort-by :name <)
         (assoc-in db [:data :github :topics]))))


(rf/reg-event-db
  :api/fetch-github-topics->failure
  (fn-traced [db [_ _error]]
    (assoc-in db [:data :github :topics] [])))


(rf/reg-event-fx
  :api/fetch-github-repositories
  (fn-traced [{{{:keys [backend-api-url]} :api} :db} _]
    {:api/fetch-github-repositories
     {:url        backend-api-url
      :on-success [:api/fetch-github-repositories->success]
      :on-failure [:api/fetch-github-repositories->failure]}}))


(rf/reg-event-fx
  :api/fetch-github-topics
  (fn-traced [{{{:keys [backend-api-url]} :api} :db} _]
    {:api/fetch-github-topics
     {:url        backend-api-url
      :on-success [:api/fetch-github-topics->success]
      :on-failure [:api/fetch-github-topics->failure]}}))


(rf/reg-sub
  :github/repositories
  (fn [db]
    (get-in db [:data :github :repositories] [])))


(rf/reg-sub
  :github/topics
  (fn [db]
    (let [topics (get-in db [:data :github :topics])]
      (reduce
        (fn [acc {:keys [name]}]
          (conj acc name))
        []
        topics))))


(rf/reg-sub
  :github/pagination
  (fn [db]
    (get-in db [:dashboard :github :pagination] [])))


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


(rf/reg-event-fx
  :github/select-tag
  (fn [{:keys [db]} [_ tags]]
    {:db (assoc-in db [:dashboard :github :selected-tags] (set tags))
     :dispatch [:github/fetch-repositories-by-tag {:page 1 :page-size 20}]}))


(rf/reg-event-fx
  :github/fetch-repositories-by-tag
  (fn-traced [{:keys [db]} [_ {:keys [page page-size]}]]
    (let [backend-api-url (get-in db [:api :backend-api-url])
          selected-tags (get-in db [:dashboard :github :selected-tags])]
      {:api/fetch-github-repositories
       {:url           backend-api-url
        :selected-tags selected-tags
        :page          page
        :per-page      page-size
        :on-success    [:api/fetch-github-repositories->success]
        :on-failure    [:api/fetch-github-repositories->failure]}})))
