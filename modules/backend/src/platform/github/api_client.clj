(ns platform.github.api-client
  (:require
    [clojure.string :as str]
    [graphql-builder.core :as gql-builder]
    [graphql-builder.parser :as gql-parser]
    [platform.common :refer [parse-int load-query build-url transform-to-kebab-keywords
                             request-graphql request-rest safe not-found-response-exception-handler]]
    [re-graph.core :as re-graph]))


(defonce ^:private settings (atom {}))


(defn- flatten-nodes-data
  [source-map]
  (->> source-map
       :edges
       (reduce
         (fn [acc {:keys [node] :as edge}]
           (let [edge' (dissoc edge :node)]
             (conj acc (merge edge' node))))
         [])))


(defn parse-repository-url
  "Extract the owner's name and the repository name from the repository url."
  [url]
  (let [url (str/replace url #".git$" "")
        re #"^(?:http(?:s)?|git)(?::\/\/|@)(?:www\.)?github\.com[\/:]([\w,\-,\_\.]+)\/([\w,\-,\_\.]+)\/?$"]
    (when-let [matches (re-matches re url)]
      (into [] (rest matches)))))


(defn- split-releases-by-asset-count
  [releases boundary]
  (group-by #(<= (get-in % [:release-assets :total-count]) boundary) releases))


(defn- split-release-ids-by-asset-count
  [releases boundary]
  (let [{batch-releases true releases false} (split-releases-by-asset-count releases boundary)]
    [(mapv :id batch-releases) (mapv :id releases)]))


(defn init
  "Initialize the module. This function must be called before calling
  all other public functions related to the GitHub API requests."
  [{:keys [service-name graphql-url oauth-token] :as config}]
  (swap! settings merge config)
  (re-graph/init service-name
                 {:http {:url  graphql-url
                         :impl {:oauth-token oauth-token
                                :method :post
                                :content-type :json}
                         :supported-operations #{:query}}
                  :ws nil})
  service-name)


(defn shutdown
  []
  (re-graph/destroy (:service-name @settings)))


(gql-parser/defgraphql graphql-queries "graphql/get_base_info.graphql"
                       "graphql/get_batch_release_assets.graphql"
                       "graphql/get_languages.graphql"
                       "graphql/get_issues.graphql"
                       "graphql/get_release_assets.graphql"
                       "graphql/get_releases.graphql"
                       "graphql/get_topics.graphql"
                       "graphql/search_repositories.graphql")


(def queries-map
  (gql-builder/query-map graphql-queries {:inline-fragments true}))


(def get-base-info-query
  (load-query queries-map :get-base-info))


(def get-batch-release-assets-query
  (load-query queries-map :get-batch-release-assets))


(def get-languages-query
  (load-query queries-map :get-languages))


(def get-issues-query
  (load-query queries-map :get-issues))


(def get-release-assets-query
  (load-query queries-map :get-release-assets))


(def get-releases-query
  (load-query queries-map :get-releases))


(def get-topics-query
  (load-query queries-map :get-topics))


(def search-repositories-query
  (load-query queries-map :search-repositories))


(defn on-github-get-base-info-handler
  [{:keys [data errors]}]
  (if-not errors
    (let [data (transform-to-kebab-keywords data)
          repository (:repository data)
          default-branch-name (get-in repository [:default-branch-ref :name])]
      (-> (assoc repository :default-branch default-branch-name)
          (dissoc :default-branch-ref)))
    (throw (ex-info "Request to GitHub failed: can't fetch base info"
                    {:errors errors}))))


(defn on-github-get-batch-release-assets-handler
  [{:keys [data errors]}]
  (if-not errors
    (let [data (transform-to-kebab-keywords data)
          releases (:nodes data)]
      (reduce
        (fn [acc {:keys [id release-assets]}]
          (conj acc {:release-id id
                     :assets (flatten-nodes-data release-assets)}))
        [] releases))
    (throw (ex-info "Request to GitHub failed: can't fetch batch release assets"
                    {:errors errors}))))


(defn on-github-get-languages-handler
  [{:keys [data errors]}]
  (if-not errors
    (let [data (transform-to-kebab-keywords data)
          languages (get-in data [:repository :languages])]
      {:languages (flatten-nodes-data languages)
       :total-count (:total-count languages)
       :page-info (:page-info languages)})
    (throw (ex-info "Request to GitHub failed: can't get languages"
                    {:errors errors}))))


(defn on-github-get-issues-handler
  [{:keys [data errors]}]
  (if-not errors
    (let [data (transform-to-kebab-keywords data)
          issues (get-in data [:repository :issues])]
      {:issues (flatten-nodes-data issues)
       :total-count (:total-count issues)
       :page-info (:page-info issues)})
    (throw (ex-info "Request to GitHub failed: can't fetch issues"
                    {:errors errors}))))


(defn on-github-get-release-assets-handler
  [{:keys [data errors]}]
  (if-not errors
    (let [data (transform-to-kebab-keywords data)
          assets (get-in data [:node :release-assets])]
      {:release-assets (flatten-nodes-data assets)
       :page-info (:page-info assets)})
    (throw (ex-info "Request to GitHub failed: can't fetch release assets"
                    {:errors errors}))))


(defn on-github-get-releases-handler
  [{:keys [data errors]}]
  (if-not errors
    (let [data (transform-to-kebab-keywords data)
          releases (get-in data [:repository :releases])]
      {:releases (flatten-nodes-data releases)
       :total-count (:total-count releases)
       :page-info (:page-info releases)})
    (throw (ex-info "Request to GitHub failed: can't fetch releases"
                    {:errors errors}))))


(defn on-github-get-topics-handler
  [{:keys [data errors]}]
  (if-not errors
    (let [data (transform-to-kebab-keywords data)
          topics (get-in data [:repository :repository-topics])]
      {:topics (reduce
                 (fn [acc {{:keys [name]} :topic}]
                   (conj acc name))
                 [] (flatten-nodes-data topics))
       :total-count (:total-count topics)
       :page-info (:page-info topics)})
    (throw (ex-info "Request to GitHub failed: can't fetch topics"
                    {:errors errors}))))


(defn on-github-get-contributor-count-handler
  [{:keys [headers errors]}]
  (if-not errors
    (if-let [link (get headers "link")]
      (-> (re-find #"page=(\d+)>; rel=\"last\"" link)
          second
          parse-int)
      1)
    (throw (ex-info "Request to GitHub failed: can't fetch contributor-count"
                    {:errors errors}))))


(defn on-github-get-community-metrics-handler
  [{:keys [data errors]}]
  (if-not errors
    data
    (throw (ex-info "Request to GitHub failed: can't fetch community-metrics"
                    {:errors errors}))))


(defn on-github-get-file-download-url-handler
  [{:keys [data errors]}]
  (if-not errors
    (:download-url data)
    (throw (ex-info "Request to GitHub failed: can't get community metrics file download url"
                    {:errors errors}))))


(defn on-github-search-repository-handler
  [{:keys [data errors]}]
  (if-not errors
    (let [data (transform-to-kebab-keywords data)
          {{:keys [repository-count page-info] :as search-result} :search} data
          urls (flatten-nodes-data search-result)]
      {:urls urls
       :total-count repository-count
       :page-info page-info})
    (throw (ex-info "Request to GitHub failed: can't fetch search results"
                    {:errors errors}))))


(defn get-objects-with-pagination
  "Retrieve a list of GitHub GraphQL API objects."
  [get-query variables handler list-key]
  (let [{:keys [service-name]} @settings
        build-query-map (fn [m] (:graphql (get-query m)))]
    (loop [{obj-list list-key page-info :page-info} (-> (build-query-map variables)
                                                        (request-graphql service-name handler))
           acc []]
      (if-not (:has-next-page page-info)
        (into [] (concat acc obj-list))
        (recur (-> (assoc variables :after-cursor (:end-cursor page-info))
                   build-query-map
                   (request-graphql service-name handler))
               (into [] (concat acc obj-list)))))))


(defn get-base-info
  "Retrieve base info from the specified repository."
  [repo-owner repo-name]
  (let [{:keys [service-name]} @settings
        {query-map :graphql} (get-base-info-query {:repo-owner repo-owner
                                                   :repo-name repo-name})]
    (request-graphql query-map service-name on-github-get-base-info-handler)))


(defn get-batch-release-assets*
  "Retrieve assets for multiple releases at once."
  ([release-ids]
   (get-batch-release-assets* release-ids 10))
  ([release-ids page-size]
   (let [{:keys [service-name]} @settings
         {query-map :graphql} (get-batch-release-assets-query {:release-ids release-ids
                                                               :page-size page-size})]
     (request-graphql query-map service-name on-github-get-batch-release-assets-handler))))


(defn get-batch-release-assets
  "Split the list of releases into batches and retrieve assets for every batch.
   Assets are retrieved for several (batch-size [default: 50]) releases at once .
   The function is applicable only to those releases whose number of assets is less
   than the assets page size. The maximum allowed page size is 100."
  ([release-ids]
   (get-batch-release-assets release-ids 50 10))
  ([release-ids batch-size]
   (get-batch-release-assets release-ids batch-size 10))
  ([release-ids batch-size assets-page-size]
   (let [chunked-release-ids (partition batch-size release-ids)]
     (-> (mapv (fn [release-ids-batch]
                 (get-batch-release-assets* (vec release-ids-batch) assets-page-size))
               chunked-release-ids)
         flatten
         vec))))


(defn get-languages
  "Retrieve languages from the specified repository."
  ([repo-owner repo-name]
   (get-languages repo-owner repo-name 20))
  ([repo-owner repo-name page-size]
   (let [variables {:repo-owner repo-owner
                    :repo-name repo-name
                    :page-size page-size}]
     (get-objects-with-pagination get-languages-query variables
                                  on-github-get-languages-handler
                                  :languages))))


(defn get-issues
  "Retrieve issues from the specified repository."
  ([repo-owner repo-name]
   (get-issues repo-owner repo-name 20))
  ([repo-owner repo-name page-size]
   (let [variables {:repo-owner repo-owner
                    :repo-name repo-name
                    :page-size page-size}]
     (get-objects-with-pagination get-issues-query variables
                                  on-github-get-issues-handler
                                  :issues))))


(defn get-release-assets
  "Retrieve assets for the specified release."
  ([release-id]
   (get-release-assets release-id 10))
  ([release-id page-size]
   (let [variables {:release-id release-id
                    :page-size page-size}]
     {:release-id release-id
      :assets (get-objects-with-pagination get-release-assets-query variables
                                           on-github-get-release-assets-handler
                                           :release-assets)})))


(defn- get-releases*
  ([repo-owner repo-name]
   (get-releases* repo-owner repo-name 20))
  ([repo-owner repo-name page-size]
   (let [variables {:repo-owner repo-owner
                    :repo-name repo-name
                    :page-size page-size}]
     (get-objects-with-pagination get-releases-query variables
                                  on-github-get-releases-handler :releases))))


(defn get-assets
  "Retrieve assets for the specified releases."
  ([releases]
   (get-assets releases 5))
  ([releases page-size]
   (let [[batch-ids ids] (split-release-ids-by-asset-count releases page-size)]
     (into (mapv #(get-release-assets % page-size) ids)
           (get-batch-release-assets batch-ids page-size)))))


(defn get-release-download-count
  "Retrieve the number of downloads for the specified release."
  [{:keys [assets] :as _release}]
  (reduce (fn [acc asset]
            (+ acc (:download-count asset)))
          0 assets))


(defn get-releases
  "Retrieve releases from the specified repository."
  [repo-owner repo-name]
  (let [releases (get-releases* repo-owner repo-name)
        assets (get-assets releases)]
    (reduce
      (fn [acc {:keys [id] :as release}]
        (let [release-assets (first (filter #(= (:release-id %) id) assets))]
          (conj acc (-> (dissoc release :release-assets)
                        (assoc :download-count (get-release-download-count release-assets))
                        (merge release-assets)
                        (dissoc :release-id)))))
      [] releases)))


(defn get-topics
  "Retrieve topics from the specified repository."
  ([repo-owner repo-name]
   (get-topics repo-owner repo-name 20))
  ([repo-owner repo-name page-size]
   (let [variables {:repo-owner repo-owner
                    :repo-name repo-name
                    :page-size page-size}]
     (get-objects-with-pagination get-topics-query variables
                                  on-github-get-topics-handler :topics))))


(defn get-total-download-count
  "Retrieve the total number of downloads for the releases."
  [releases]
  (reduce (fn [acc release]
            (+ acc (:download-count release)))
          0 releases))


(defn get-contributor-count
  "Retrieve the number contributors from the specified repository."
  ([repo-owner repo-name]
   (get-contributor-count repo-owner repo-name false))
  ([repo-owner repo-name include-anon]
   (let [{:keys [rest-url oauth-token]} @settings
         params {:per_page 1 :anon include-anon}]
     (safe
       (-> (build-url rest-url "repos" repo-owner repo-name "contributors")
           (request-rest on-github-get-contributor-count-handler
                         {:per_page 1
                          :anon include-anon
                          :oauth-token oauth-token
                          :query-params params}))
       #(not-found-response-exception-handler % "Failed to fetch contributor count!")))))


(defn get-community-metrics
  "Retrieve overall health score, repository description, the presence of documentation,
   detected code of conduct, detected license, and the presence of ISSUE_TEMPLATE,
   PULL_REQUEST_TEMPLATE, README, and CONTRIBUTING files."
  [repo-owner repo-name]
  (let [{:keys [rest-url oauth-token]} @settings]
    (safe
      (-> (build-url rest-url "repos" repo-owner repo-name "community/profile")
          (request-rest on-github-get-community-metrics-handler {:oauth-token oauth-token}))
      #(not-found-response-exception-handler % "Failed to fetch metrics!"))))


(defn get-file-download-url
  "Retrieve the file download URL."
  [file-info-url]
  (let [{:keys [oauth-token]} @settings]
    (request-rest file-info-url on-github-get-file-download-url-handler {:oauth-token oauth-token})))


(defn get-file
  "Try to download a file by the specified URL and log an error in case of failure."
  [url]
  (safe
    (slurp (get-file-download-url url))
    #(let [error-message (str "Failed to download file: " url)]
       (not-found-response-exception-handler % error-message))))


(defn get-documentation
  "Download the contents of doc files (based on API urls from the community metrics)."
  [{files-info :files :as _community-metrics}]
  (let [files-info (select-keys files-info [:readme :contributing
                                            :issue-template :pull-request-template
                                            :code-of-conduct-file])]
    (reduce-kv (fn [acc key {:keys [url]}]
                 (if url
                   (assoc acc key (get-file url))
                   acc))
               {} files-info)))


(defn search-repositories
  "Search for repositories by specified conditions in the `search-query` variable."
  ([search-query]
   (search-repositories search-query 20))
  ([search-query page-size]
   (let [variables {:search-query search-query
                    :page-size page-size}]
     (get-objects-with-pagination search-repositories-query variables
                                  on-github-search-repository-handler
                                  :urls))))


(defn get-repository-info
  "Retrieve information (number of stars, documentation health, documentation files,
   list of releases, list of issues, number of contributors, license info, full name,
   homepage url, mirror url, description, short description (HTML), primary language with color,
   creation date, date of the last update, fork status, mirror status, archived status,
   locked status, lock reason, wiki enabled status,number of forks) about the repository
   by the GitHub url."
  [url]
  (let [[repo-owner repo-name] (parse-repository-url url)
        metrics (get-community-metrics repo-owner repo-name)
        releases (get-releases repo-owner repo-name)]
    (merge (get-base-info repo-owner repo-name)
           {:contributor-count (get-contributor-count repo-owner repo-name)
            :metrics (select-keys metrics [:health-percentage :updated-at])
            :doc-files (get-documentation metrics)
            :languages (get-languages repo-owner repo-name)
            :issues (get-issues repo-owner repo-name)
            :releases releases
            :topics (get-topics repo-owner repo-name)
            :download-count (get-total-download-count releases)})))
