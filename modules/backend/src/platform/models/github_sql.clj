(ns platform.models.github-sql
  (:require
    [clojure.instant :as instant]
    [clojure.string :as str]
    [honey.sql :as sql]
    [honey.sql.helpers :as helpers]
    [next.jdbc :as jdbc]
    [next.jdbc.date-time :as date-time]))


(defn- insert-license!
  [tx {:keys [name nickname url pseudo-license] :as license}]
  (jdbc/execute! tx (-> (helpers/insert-into :license)
                        (helpers/values [{:id (java.util.UUID/randomUUID)
                                          :name name
                                          :nick_name nickname
                                          :url url
                                          :is_pseudo_license pseudo-license}])
                        (helpers/on-conflict :name)
                        (helpers/do-update-set :name)
                        (helpers/returning :id)
                        sql/format)))


(defn- insert-repository!
  [tx license-id {:keys [name-with-owner homepage-url description
                         short-description-html default-branch
                         created-at updated-at fork-count stargazer-count
                         contributor-count download-count is-mirror mirror_url
                         is-archived is-fork has-wiki-enabled is-locked
                         doc-files metrics] :as repository}]
  (let [[owner name] (str/split name-with-owner #"/")
        {:keys [readme contributing code-of-conduct issue-template pull-request-template]} doc-files
        {metrics-health-percentage :health-percentage  metrics-updated-at :updated-at} metrics]
    (jdbc/execute! tx (-> (helpers/insert-into :repository)
                          (helpers/values [{:id (java.util.UUID/randomUUID)
                                            :owner owner
                                            :name name
                                            :home_page homepage-url
                                            :description description
                                            :short_description_html short-description-html
                                            :default_branch default-branch
                                            :created_at (some-> created-at
                                                                instant/read-instant-date)
                                            :updated_at (some-> updated-at
                                                                instant/read-instant-date)
                                            :fork_count fork-count
                                            :stargazer_count stargazer-count
                                            :contributor_count contributor-count
                                            :total_downloads download-count
                                            :is_mirror is-mirror
                                            :mirror_url mirror_url
                                            :is_archived is-archived
                                            :is_fork is-fork
                                            :has_wiki_enabled has-wiki-enabled
                                            :is_locked is-locked
                                            :contributing contributing
                                            :readme readme
                                            :code_of_conduct code-of-conduct
                                            :issue_template issue-template
                                            :pull_request_template pull-request-template
                                            :documentation_health metrics-health-percentage
                                            :health_state_updated_at (some-> metrics-updated-at
                                                                             instant/read-instant-date)
                                            :license_id license-id}])
                          (helpers/returning :id)
                          sql/format))))


(defn- prepare-issue
  [repository-id {:keys [title created-at url closed closed-at] :as issue}]
  {:id (java.util.UUID/randomUUID)
   :title title
   :created_at (some-> created-at
                       instant/read-instant-date)
   :url url
   :closed closed
   :closed_at (some-> closed-at
                      instant/read-instant-date)
   :repository_id repository-id})


(defn- insert-issues!
  [tx repository-id issues]
  (let [prepared-issues (mapv #(prepare-issue repository-id %) issues)]
    (when-not (empty? prepared-issues)
      (jdbc/execute! tx (-> (helpers/insert-into :issue)
                            (helpers/values prepared-issues)
                            sql/format)))))


(defn insert-release!
  [tx repository-id {:keys [name tag-name created-at download-count] :as release}]
  (jdbc/execute! tx (-> (helpers/insert-into :release)
                        (helpers/values [{:id (java.util.UUID/randomUUID)
                                          :name name
                                          :tag_name tag-name
                                          :created_at (some-> created-at
                                                              instant/read-instant-date)
                                          :downloads download-count
                                          :repository_id repository-id}])
                        (helpers/returning :id)
                        sql/format)))


(defn- prepare-asset
  [release-id {:keys [name download-count] :as asset}]
  {:id (java.util.UUID/randomUUID)
   :name name
   :downloads download-count
   :release_id release-id})


(defn- insert-assets!
  [tx release-id assets]
  (let [prepared-assets (mapv #(prepare-asset release-id %) assets)]
    (when-not (empty? prepared-assets)
      (jdbc/execute! tx (-> (helpers/insert-into :asset)
                            (helpers/values prepared-assets)
                            sql/format)))))


(defn- insert-releases!
  [tx repository-id releases]
  (when-not (empty? releases)
    (doall
      (map (fn [{:keys [assets] :as release}]
             (let [[{release-id :release/id}] (insert-release! tx repository-id release)]
               (insert-assets! tx release-id assets)))
           releases))))


(defn insert-topic!
  [tx topic-name]
  (jdbc/execute! tx (-> (helpers/insert-into :topic)
                        (helpers/values [{:id (java.util.UUID/randomUUID)
                                          :name topic-name}])
                        (helpers/on-conflict :name)
                        (helpers/do-update-set :name)
                        (helpers/returning :id)
                        sql/format)))


(defn insert-repository-topic!
  [tx repository-id topic-id]
  (jdbc/execute! tx (-> (helpers/insert-into :repository_topic)
                        (helpers/values [{:repository_id repository-id
                                          :topic_id topic-id}])
                        sql/format)))


(defn- insert-topics!
  [tx repository-id topics]
  (when-not (empty? topics)
    (doall
      (map (fn [topic]
             (let [[{topic-id :topic/id}] (insert-topic! tx topic)]
               (insert-repository-topic! tx repository-id topic-id)))
           topics))))


(defn- insert-language!
  [tx {:keys [name color] :as language}]
  (jdbc/execute! tx (-> (helpers/insert-into :language)
                        (helpers/values [{:id (java.util.UUID/randomUUID)
                                          :name name
                                          :color color}])
                        (helpers/on-conflict :name)
                        (helpers/do-update-set :name)
                        (helpers/returning :id)
                        sql/format)))


(defn- insert-repository-language!
  [tx primary-language? size repository-id language-id]
  (jdbc/execute! tx (-> (helpers/insert-into :repository_language)
                        (helpers/values [{:repository_id repository-id
                                          :language_id language-id
                                          :size size
                                          :is_primary_language primary-language?}])
                        sql/format)))


(defn- insert-languages!
  [tx repository-id {primary-language-name :name :as primary-language} languages]
  (when-not (empty? languages)
    (doall
      (map (fn [{:keys [name size] :as language}]
             (let [[{language-id :language/id}] (insert-language! tx language)
                   primary-language? (= name primary-language-name)]
               (insert-repository-language! tx primary-language? size repository-id language-id)))
           languages))))


(defn insert-repository-info!
  [datasource repository-info]
  (jdbc/with-transaction [tx datasource]
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
  [datasource owner name]
  (with-open [conn (jdbc/get-connection datasource)]
    (let [sqlmap {:select [[[:exists {:select [1]
                                      :from [:repository]
                                      :where [:and
                                              [:= :owner owner]
                                              [:= :name name]]}]
                            :repository_exists]]}]
      (-> (jdbc/execute! conn (sql/format sqlmap))
          first
          :repository_exists))))


(defn update-repository-info!
  [datasource repository-info]
  (jdbc/with-transaction [tx datasource]))
