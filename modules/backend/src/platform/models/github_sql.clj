(ns platform.models.github-sql
  (:require
    [clojure.instant :as instant]
    [clojure.string :as str]
    [honey.sql :as sql]
    [honey.sql.helpers :as helpers]
    [next.jdbc :as jdbc]
    [next.jdbc.date-time])
  (:import
    (java.util
      UUID)))


(defn- insert-license!
  [db {:keys [name nickname url pseudo-license] :as _license}]
  (jdbc/execute! db (-> (helpers/insert-into :license)
                        (helpers/values [{:id                (UUID/randomUUID)
                                          :name              name
                                          :nick_name         nickname
                                          :url               url
                                          :is_pseudo_license pseudo-license}])
                        (helpers/on-conflict :name)
                        (helpers/do-update-set :name)
                        (helpers/returning :id)
                        sql/format)))


(defn- insert-repository!
  [db license-id {:keys [name-with-owner homepage-url description
                         short-description-html default-branch
                         created-at updated-at fork-count stargazer-count
                         contributor-count download-count is-mirror mirror_url
                         is-archived is-fork has-wiki-enabled is-locked
                         doc-files metrics] :as _repository}]
  (let [[owner name] (str/split name-with-owner #"/")
        {:keys [readme contributing code-of-conduct issue-template pull-request-template]} doc-files
        {metrics-health-percentage :health-percentage metrics-updated-at :updated-at} metrics]
    (jdbc/execute! db (-> (helpers/insert-into :repository)
                          (helpers/values [{:id                      (UUID/randomUUID)
                                            :owner                   owner
                                            :name                    name
                                            :home_page               homepage-url
                                            :description             description
                                            :short_description_html  short-description-html
                                            :default_branch          default-branch
                                            :created_at              (some-> created-at
                                                                             instant/read-instant-date)
                                            :updated_at              (some-> updated-at
                                                                             instant/read-instant-date)
                                            :fork_count              fork-count
                                            :stargazer_count         stargazer-count
                                            :contributor_count       contributor-count
                                            :total_downloads         download-count
                                            :is_mirror               is-mirror
                                            :mirror_url              mirror_url
                                            :is_archived             is-archived
                                            :is_fork                 is-fork
                                            :has_wiki_enabled        has-wiki-enabled
                                            :is_locked               is-locked
                                            :contributing            contributing
                                            :readme                  readme
                                            :code_of_conduct         code-of-conduct
                                            :issue_template          issue-template
                                            :pull_request_template   pull-request-template
                                            :documentation_health    metrics-health-percentage
                                            :health_state_updated_at (some-> metrics-updated-at
                                                                             instant/read-instant-date)
                                            :license_id              license-id}])
                          (helpers/returning :id)
                          sql/format))))


(defn- prepare-issue
  [repository-id {:keys [title created-at url closed closed-at] :as _issue}]
  {:id            (UUID/randomUUID)
   :title         title
   :created_at    (some-> created-at
                          instant/read-instant-date)
   :url           url
   :closed        closed
   :closed_at     (some-> closed-at
                          instant/read-instant-date)
   :repository_id repository-id})


(defn- insert-issues!
  [db repository-id issues]
  (let [prepared-issues (mapv #(prepare-issue repository-id %) issues)]
    (when-not (empty? prepared-issues)
      (jdbc/execute! db (-> (helpers/insert-into :issue)
                            (helpers/values prepared-issues)
                            sql/format)))))


(defn insert-release!
  [db repository-id {:keys [name tag-name created-at download-count] :as _release}]
  (jdbc/execute! db (-> (helpers/insert-into :release)
                        (helpers/values [{:id            (UUID/randomUUID)
                                          :name          name
                                          :tag_name      tag-name
                                          :created_at    (some-> created-at
                                                                 instant/read-instant-date)
                                          :downloads     download-count
                                          :repository_id repository-id}])
                        (helpers/returning :id)
                        sql/format)))


(defn- prepare-asset
  [release-id {:keys [name download-count] :as _asset}]
  {:id         (UUID/randomUUID)
   :name       name
   :downloads  download-count
   :release_id release-id})


(defn- insert-assets!
  [db release-id assets]
  (let [prepared-assets (mapv #(prepare-asset release-id %) assets)]
    (when-not (empty? prepared-assets)
      (jdbc/execute! db (-> (helpers/insert-into :asset)
                            (helpers/values prepared-assets)
                            sql/format)))))


(defn- insert-releases!
  [db repository-id releases]
  (when-not (empty? releases)
    (doall
      (map (fn [{:keys [assets] :as release}]
             (let [[{release-id :release/id}] (insert-release! db repository-id release)]
               (insert-assets! db release-id assets)))
           releases))))


(defn insert-topic!
  [db topic-name]
  (jdbc/execute! db (-> (helpers/insert-into :topic)
                        (helpers/values [{:id   (UUID/randomUUID)
                                          :name topic-name}])
                        (helpers/on-conflict :name)
                        (helpers/do-update-set :name)
                        (helpers/returning :id)
                        sql/format)))


(defn insert-repository-topic!
  [db repository-id topic-id]
  (jdbc/execute! db (-> (helpers/insert-into :repository_topic)
                        (helpers/values [{:repository_id repository-id
                                          :topic_id      topic-id}])
                        sql/format)))


(defn- insert-topics!
  [db repository-id topics]
  (when-not (empty? topics)
    (doall
      (map (fn [topic]
             (let [[{topic-id :topic/id}] (insert-topic! db topic)]
               (insert-repository-topic! db repository-id topic-id)))
           topics))))


(defn- insert-language!
  [db {:keys [name color] :as _language}]
  (jdbc/execute! db (-> (helpers/insert-into :language)
                        (helpers/values [{:id    (UUID/randomUUID)
                                          :name  name
                                          :color color}])
                        (helpers/on-conflict :name)
                        (helpers/do-update-set :name)
                        (helpers/returning :id)
                        sql/format)))


(defn- insert-repository-language!
  [db primary-language? size repository-id language-id]
  (jdbc/execute! db (-> (helpers/insert-into :repository_language)
                        (helpers/values [{:repository_id       repository-id
                                          :language_id         language-id
                                          :size                size
                                          :is_primary_language primary-language?}])
                        sql/format)))


(defn- insert-languages!
  [db repository-id {primary-language-name :name :as _primary-language} languages]
  (when-not (empty? languages)
    (doall
      (map (fn [{:keys [name size] :as language}]
             (let [[{language-id :language/id}] (insert-language! db language)
                   primary-language? (= name primary-language-name)]
               (insert-repository-language! db primary-language? size repository-id language-id)))
           languages))))


(defn insert-repository-info!
  [db repository-info]
  (jdbc/with-transaction [tx db]
    (let [[{license-id :license/id}] (some->> (:license-info repository-info)
                                              (insert-license! tx))
          [{repository-id :repository/id}] (insert-repository! tx license-id repository-info)]
      (insert-issues! tx repository-id (:issues repository-info))
      (insert-releases! tx repository-id (:releases repository-info))
      (insert-topics! tx repository-id (:topics repository-info))
      (insert-languages! tx repository-id
                         (:primary-language repository-info)
                         (:languages repository-info)))))


(defn repository-exists?
  [db owner name]
  (let [sqlmap {:select [1]
                :from   [:repository]
                :where  [:and
                         [:= :owner owner]
                         [:= :name name]]}]
    (->> (sql/format sqlmap)
         (jdbc/execute-one! db)
         (boolean))))
