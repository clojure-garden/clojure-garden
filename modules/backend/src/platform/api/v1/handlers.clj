(ns platform.api.v1.handlers
  (:require
    [platform.api.v1.pagination :as pagination]
    [platform.common :as common]
    [platform.github.repository :as github-repository]
    [platform.github.topics :as github-topic]))


(defn get-repositories-handler
  [{:as req :keys [params query-string uri]}]
  (let [datasource       (get-in req [:platform.system/ctx :db :datasource])
        query            (get-in req [:parameters :query])
        filters          (-> (select-keys params [:topics :is-fork])
                             (common/update-if-contains :topics common/singleton->vector))
        current-page     (get query :page 1)
        per-page         (get query :per-page 50)
        with-meta        (get query :with-meta)
        offset           (* (dec current-page) per-page)
        repository-count (github-repository/count-repositories datasource filters)
        repositories     (github-repository/get-repositories datasource filters per-page offset)
        meta             (-> (get-in req [:headers "host"])
                             (pagination/build-pagination-meta uri query-string current-page per-page repository-count))]

    {:status  200
     :headers {"Link" (pagination/build-link-header (:links meta))}
     :body
     (cond-> {:repositories repositories}
       with-meta (merge {:_meta meta}))}))


(defn get-topics-handler
  [{:as req :keys [query-string uri]}]
  (let [datasource       (get-in req [:platform.system/ctx :db :datasource])
        query            (get-in req [:parameters :query])
        current-page     (get query :page 1)
        per-page         (get query :per-page 50)
        with-meta        (get query :with-meta)
        offset           (* (dec current-page) per-page)
        topics-count     (github-topic/count-topics datasource)
        topics           (github-topic/get-topics datasource per-page offset)
        meta             (-> (get-in req [:headers "host"])
                             (pagination/build-pagination-meta uri query-string current-page per-page topics-count))]
    {:status 200
     :headers {"Link" (pagination/build-link-header (:links meta))}
     :body (cond-> {:topics topics}
             with-meta (merge {:_meta meta}))}))
