(ns platform.models.clojars-sql
  (:require
    [honey.sql :as sql]
    [honey.sql.helpers :as helpers]
    [next.jdbc :as jdbc]))


(defn- insert-artifact!
  [tx {:keys [artifact-id group-id homepage description owner
              latest-version latest-release downloads from-clojars] :as artifact}]
  (jdbc/execute! tx (->  (helpers/insert-into :artifact)
                         (helpers/values [{:id (java.util.UUID/randomUUID)
                                           :artifact_id artifact-id
                                           :group_id group-id
                                           :homepage homepage
                                           :description description
                                           :owner owner
                                           :latest_version latest-version
                                           :latest_release latest-release
                                           :downloads downloads
                                           :from_clojars from-clojars}])
                         (helpers/returning :id)
                         sql/format)))


(defn- insert-license!
  [tx {:keys [name url] :as license}]
  (jdbc/execute! tx (-> (helpers/insert-into :license)
                        (helpers/values [{:id (java.util.UUID/randomUUID)
                                          :name name
                                          :url url}])
                        (helpers/on-conflict :name)
                        (helpers/do-update-set :name)
                        (helpers/returning :id)
                        sql/format)))


(defn- insert-version-license!
  [tx version-id license-id]
  (jdbc/execute! tx (-> (helpers/insert-into :version_license)
                        (helpers/values [{:version_id version-id
                                          :license_id license-id}])
                        sql/format)))


(defn- insert-licenses!
  [tx version-id licenses]
  (when-not (empty? licenses)
    (doall
      (map (fn [license]
             (let [[{license-id :license/id}] (insert-license! tx license)]
               (insert-version-license! tx version-id license-id)))
           licenses))))


(defn- insert-version!
  [tx artifact-id {:keys [version downloads] :as artifact-version}]
  (jdbc/execute! tx (-> (helpers/insert-into :version)
                        (helpers/values [{:id (java.util.UUID/randomUUID)
                                          :name version
                                          :downloads downloads
                                          :artifact_id artifact-id}])
                        (helpers/returning :id)
                        sql/format)))


(defn- insert-version-info!
  [tx artifact-id {:keys [licenses dependencies] :as version-info}]
  (let [[{version-id :version/id}] (insert-version! tx artifact-id version-info)]
    (insert-licenses! tx version-id licenses)))


(defn- insert-versions!
  [tx artifact-id versions]
  (when-not (empty? versions)
    (doall
      (map #(insert-version-info! tx artifact-id %) versions))))


(defn insert-artifact-info!
  [datasource artifact-info from-clojars]
  (jdbc/with-transaction [tx datasource]
                         (let [artifact-info (conj artifact-info [:from-clojars from-clojars])
                               [{artifact-id :artifact/id}] (insert-artifact! tx artifact-info)]
                           (insert-versions! tx artifact-id (:recent-versions artifact-info)))))


(defn artifact-exists?
  [group-id artifact-id datasource]
  (with-open [conn (jdbc/get-connection datasource)]
    (let [sqlmap {:select [[[:exists {:select [1]
                                      :from [:artifact]
                                      :where [:and
                                              [:= :group_id group-id]
                                              [:= :artifact_id artifact-id]]}]
                            :artifact_exists]]}]
      (-> (jdbc/execute! conn (sql/format sqlmap))
          first
          :artifact_exists))))


(defn get-github-urls
  [datasource]
  (with-open [conn (jdbc/get-connection datasource)]
    (let [sqlmap {:select [:homepage]
                  :from [:artifact]
                  :where [:like :homepage "%github%"]}]
      (->> (jdbc/execute! conn (sql/format sqlmap))
           (reduce (fn [acc {url :artifact/homepage} ] (conj acc url)) [])))))
