(ns platform.github.github-sql
  (:require
    [clojure.instant :as instant]
    [clojure.string :as str]
    [honey.sql.helpers :as helpers]
    [platform.storage.jdbc-wrappers :as jw])
  (:import
    (com.zaxxer.hikari
      HikariDataSource)
    (java.util
      UUID)))


(defn- insert-license!
  [^HikariDataSource db {:as _license :keys [name nickname url pseudo-license]}]
  (jw/execute! db (-> (helpers/insert-into :license)
                      (helpers/values [{:id                (UUID/randomUUID)
                                        :name              name
                                        :nick-name         nickname
                                        :url               url
                                        :is-pseudo-license pseudo-license}])
                      (helpers/on-conflict :name)
                      (helpers/do-update-set :name)
                      (helpers/returning :id)
                      jw/sql-format)))


(defn- insert-repository!
  [^HikariDataSource db license-id {:as _repository :keys [name-with-owner homepage-url description
                                                           short-description-html default-branch
                                                           created-at updated-at fork-count stargazer-count
                                                           contributor-count download-count is-mirror mirror-url
                                                           is-archived is-fork has-wiki-enabled is-locked
                                                           doc-files metrics]}]
  (let [[owner name] (str/split name-with-owner #"/")
        {:keys [readme contributing code-of-conduct issue-template pull-request-template]} doc-files
        {metrics-health-percentage :health-percentage metrics-updated-at :updated-at} metrics]
    (jw/execute! db (-> (helpers/insert-into :repository)
                        (helpers/values [{:id                      (UUID/randomUUID)
                                          :owner                   owner
                                          :name                    name
                                          :home-page               homepage-url
                                          :description             description
                                          :short-description-html  short-description-html
                                          :default-branch          default-branch
                                          :created-at              (some-> created-at
                                                                           instant/read-instant-date)
                                          :updated-at              (some-> updated-at
                                                                           instant/read-instant-date)
                                          :fork-count              fork-count
                                          :stargazer-count         stargazer-count
                                          :contributor-count       contributor-count
                                          :total-downloads         download-count
                                          :is-mirror               is-mirror
                                          :mirror-url              mirror-url
                                          :is-archived             is-archived
                                          :is-fork                 is-fork
                                          :has-wiki-enabled        has-wiki-enabled
                                          :is-locked               is-locked
                                          :contributing            contributing
                                          :readme                  readme
                                          :code-of-conduct         code-of-conduct
                                          :issue-template          issue-template
                                          :pull-request-template   pull-request-template
                                          :documentation-health    metrics-health-percentage
                                          :health-state-updated-at (some-> metrics-updated-at
                                                                           instant/read-instant-date)
                                          :license-id              license-id}])
                        (helpers/returning :id)
                        jw/sql-format))))


(defn- prepare-issue
  [repository-id {:as _issue :keys [title created-at url closed closed-at]}]
  {:id            (UUID/randomUUID)
   :title         title
   :created-at    (some-> created-at
                          instant/read-instant-date)
   :url           url
   :closed        closed
   :closed-at     (some-> closed-at
                          instant/read-instant-date)
   :repository-id repository-id})


(defn- insert-issues!
  [^HikariDataSource db repository-id issues]
  (let [prepared-issues (mapv #(prepare-issue repository-id %) issues)]
    (when-not (empty? prepared-issues)
      (jw/execute! db (-> (helpers/insert-into :issue)
                          (helpers/values prepared-issues)
                          jw/sql-format)))))


(defn insert-release!
  [^HikariDataSource db repository-id {:as _release :keys [name tag-name created-at download-count]}]
  (jw/execute! db (-> (helpers/insert-into :release)
                      (helpers/values [{:id            (UUID/randomUUID)
                                        :name          name
                                        :tag-name      tag-name
                                        :created-at    (some-> created-at
                                                               instant/read-instant-date)
                                        :downloads     download-count
                                        :repository-id repository-id}])
                      (helpers/returning :id)
                      jw/sql-format)))


(defn- prepare-asset
  [release-id {:as _asset :keys [name download-count]}]
  {:id         (UUID/randomUUID)
   :name       name
   :downloads  download-count
   :release-id release-id})


(defn- insert-assets!
  [^HikariDataSource db release-id assets]
  (let [prepared-assets (mapv #(prepare-asset release-id %) assets)]
    (when-not (empty? prepared-assets)
      (jw/execute! db (-> (helpers/insert-into :asset)
                          (helpers/values prepared-assets)
                          jw/sql-format)))))


(defn- insert-releases!
  [^HikariDataSource db repository-id releases]
  (when-not (empty? releases)
    (doall
      (map (fn [{:as release :keys [assets]}]
             (let [[{release-id :id}] (insert-release! db repository-id release)]
               (insert-assets! db release-id assets)))
           releases))))


(defn insert-topic!
  [^HikariDataSource db topic-name]
  (jw/execute! db (-> (helpers/insert-into :topic)
                      (helpers/values [{:id   (UUID/randomUUID)
                                        :name topic-name}])
                      (helpers/on-conflict :name)
                      (helpers/do-update-set :name)
                      (helpers/returning :id)
                      jw/sql-format)))


(defn insert-repository-topic!
  [^HikariDataSource db repository-id topic-id]
  (jw/execute! db (-> (helpers/insert-into :repository-topic)
                      (helpers/values [{:repository-id repository-id
                                        :topic-id      topic-id}])
                      jw/sql-format)))


(defn- insert-topics!
  [^HikariDataSource db repository-id topics]
  (when-not (empty? topics)
    (doall
      (map (fn [topic]
             (let [[{topic-id :id}] (insert-topic! db topic)]
               (insert-repository-topic! db repository-id topic-id)))
           topics))))


(defn- insert-language!
  [^HikariDataSource db {:as _language :keys [name color]}]
  (jw/execute! db (-> (helpers/insert-into :language)
                      (helpers/values [{:id    (UUID/randomUUID)
                                        :name  name
                                        :color color}])
                      (helpers/on-conflict :name)
                      (helpers/do-update-set :name)
                      (helpers/returning :id)
                      jw/sql-format)))


(defn- insert-repository-language!
  [^HikariDataSource db primary-language? size repository-id language-id]
  (jw/execute! db (-> (helpers/insert-into :repository-language)
                      (helpers/values [{:repository-id       repository-id
                                        :language-id         language-id
                                        :size                size
                                        :is-primary-language primary-language?}])
                      jw/sql-format)))


(defn- insert-languages!
  [^HikariDataSource db repository-id {:as _primary-language primary-language-name :name} languages]
  (when-not (empty? languages)
    (doall
      (map (fn [{:keys [name size] :as language}]
             (let [[{language-id :id}] (insert-language! db language)
                   primary-language? (= name primary-language-name)]
               (insert-repository-language! db primary-language? size repository-id language-id)))
           languages))))


(defn insert-repository-info!
  [^HikariDataSource db repository-info]
  (jw/with-transaction [tx db]
    (let [[{license-id :id}] (some->> (:license-info repository-info)
                                      (insert-license! tx))
          [{repository-id :id}] (insert-repository! tx license-id repository-info)]
      (insert-issues! tx repository-id (:issues repository-info))
      (insert-releases! tx repository-id (:releases repository-info))
      (insert-topics! tx repository-id (:topics repository-info))
      (insert-languages! tx repository-id
                         (:primary-language repository-info)
                         (:languages repository-info)))))


(defn repository-exists?
  [^HikariDataSource db owner name]
  (let [sqlmap {:select [1]
                :from   [:repository]
                :where  [:and
                         [:= :owner owner]
                         [:= :name name]]}]
    (->> (jw/sql-format sqlmap)
         (jw/execute-one! db)
         (boolean))))


(defn select-repositories
  ([^HikariDataSource db]
   (select-repositories db nil nil nil))
  ([db filters]
   (select-repositories db filters nil nil))
  ([db {:as _filters :keys [topics]} limit offset]
   (let [sql-map {:select (if offset [:*] [[:%count.* :aggregate]])
                  :from [[(cond-> {:select-distinct [:repository/id
                                                     :repository/owner
                                                     :repository/name
                                                     :repository/home-page
                                                     :repository/description
                                                     :repository/default-branch
                                                     :repository/created-at
                                                     :repository/updated-at
                                                     :repository/fork-count
                                                     :repository/stargazer-count
                                                     :repository/contributor-count
                                                     :repository/total-downloads
                                                     :repository/is-mirror
                                                     :repository/mirror-url
                                                     :repository/is-archived
                                                     :repository/is-fork
                                                     :repository/has-wiki-enabled
                                                     :repository/is-locked
                                                     :repository/lock-reason
                                                     :repository/contributing
                                                     :repository/readme :repository/code-of-conduct
                                                     :repository/issue-template
                                                     :repository/pull-request-template
                                                     :repository/documentation-health
                                                     :repository/health-state-updated-at
                                                     [:license/name :license-name]
                                                     [:license/url :license-url]
                                                     [:license/is-pseudo-license]]
                                   :from            [:repository]
                                   :left-join       [:license [:= :repository/license-id :license/id]]}
                            topics (merge {:join  [[:repository-topic :rt] [:= :repository/id :rt/repository-id]
                                                   :topic                 [:= :rt/topic-id :topic/id]]
                                           :where [:and
                                                   [:in :topic/name topics]
                                                   [:= :repository/is-fork false]]})
                            :always (merge {:order-by [[:repository/owner :asc]
                                                       [:repository/name :asc]]})
                            (and limit offset) (merge {:limit limit
                                                       :offset offset})) :repositories]]}]
     (->> (jw/sql-format sql-map)
          (jw/execute! db)))))


(defn select-repository-issues
  ([^HikariDataSource db repository-id]
   (select-repository-issues db repository-id nil nil))
  ([^HikariDataSource db repository-id offset limit]
   (let [sql-map (cond-> {:select    [:issue/id :issue/title :issue/created-at
                                      :issue/url :issue/closed :issue/closed-at]
                          :from      [:issue]
                          :where     [:= :issue/repository-id repository-id]
                          :order-by  [[:issue/created-at :asc]]}
                   (and limit offset) (merge {:limit limit
                                              :offset offset}))]
     (->> (jw/sql-format sql-map)
          (jw/execute! db)))))


(defn select-topics
  ([^HikariDataSource db]
   (select-topics db nil nil))
  ([^HikariDataSource db limit offset]
   (let [sql-map {:select (if offset [:*] [[:%count.* :aggregate]])
                  :from [[(cond-> {:select    [:topic/name [:%count.rt/repository-id :repository_count]]
                                   :from      [:topic]
                                   :left-join [[:repository-topic :rt] [:= :topic/id :rt/topic-id]]
                                   :group-by  [:topic/name]
                                   :order-by  [[:topic/name :asc]]}
                            (and limit offset) (merge {:limit limit
                                                       :offset offset})) :topics]]}]
     (->> (jw/sql-format sql-map)
          (jw/execute! db)))))


(defn select-topics-by-id
  [^HikariDataSource db repository-id]
  (let [sql-map {:select    [[:topic/name :name]]
                 :from      [:repository-topic]
                 :left-join [:topic [:= :repository-topic/topic-id :topic/id]]
                 :where     [:= :repository-topic/repository-id repository-id]
                 :order-by [[:topic/name :asc]]}]
    (->> (jw/sql-format sql-map)
         (jw/execute! db))))


(defn select-languages-by-id
  [^HikariDataSource db repository-id]
  (let [sql-map {:select    [[:language/name :name]
                             [:language/color :color]
                             [:repository-language/size :size]
                             [:repository-language/is-primary-language :is-primary-language]]
                 :from      [:repository-language]
                 :left-join [:language [:= :repository-language/language-id :language/id]]
                 :where     [:= :repository-language/repository-id repository-id]
                 :order-by  [[:language/name :asc]]}]
    (->> (jw/sql-format sql-map)
         (jw/execute! db))))
