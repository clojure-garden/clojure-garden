(ns platform.models.clojars-sql
  (:require
    [honey.sql :as sql]
    [honey.sql.helpers :as helpers]
    [next.jdbc :as jdbc])
  (:import
    (java.util
      UUID)))


(defn- insert-artifact!
  [db {:keys [artifact-id group-id homepage description owner
              latest-version latest-release downloads from-clojars] :as _artifact}]
  (jdbc/execute! db (-> (helpers/insert-into :artifact)
                        (helpers/values [{:id             (UUID/randomUUID)
                                          :artifact_id    artifact-id
                                          :group_id       group-id
                                          :homepage       homepage
                                          :description    description
                                          :owner          owner
                                          :latest_version latest-version
                                          :latest_release latest-release
                                          :downloads      downloads
                                          :from_clojars   from-clojars}])
                        (helpers/returning :id)
                        sql/format)))


(defn- insert-license!
  [db {:keys [name url] :as _license}]
  (jdbc/execute! db (-> (helpers/insert-into :license)
                        (helpers/values [{:id   (UUID/randomUUID)
                                          :name name
                                          :url  url}])
                        (helpers/on-conflict :name)
                        (helpers/do-update-set :name)
                        (helpers/returning :id)
                        sql/format)))


(defn- insert-version-license!
  [db version-id license-id]
  (jdbc/execute! db (-> (helpers/insert-into :version_license)
                        (helpers/values [{:version_id version-id
                                          :license_id license-id}])
                        sql/format)))


(defn- insert-licenses!
  [db version-id licenses]
  (when-not (empty? licenses)
    (doall
      (map (fn [license]
             (let [[{license-id :license/id}] (insert-license! db license)]
               (insert-version-license! db version-id license-id)))
           licenses))))


(defn- insert-version!
  [db artifact-id {:keys [version downloads] :as _artifact-version}]
  (jdbc/execute! db (-> (helpers/insert-into :version)
                        (helpers/values [{:id          (UUID/randomUUID)
                                          :name        version
                                          :downloads   downloads
                                          :artifact_id artifact-id}])
                        (helpers/returning :id)
                        sql/format)))


(defn- insert-version-info!
  [db artifact-id {:keys [licenses] :as version-info}]
  (let [[{version-id :version/id}] (insert-version! db artifact-id version-info)]
    (insert-licenses! db version-id licenses)))


(defn- insert-versions!
  [db artifact-id versions]
  (when-not (empty? versions)
    (doall
      (map #(insert-version-info! db artifact-id %) versions))))


(defn insert-artifact-info!
  [db artifact-info from-clojars]
  (jdbc/with-transaction [tx db]
    (let [artifact-info (conj artifact-info [:from-clojars from-clojars])
          [{artifact-id :artifact/id}] (insert-artifact! tx artifact-info)]
      (insert-versions! tx artifact-id (:recent-versions artifact-info)))))


(defn artifact-exists?
  [db group-id artifact-id]
  (let [sqlmap {:select [1]
                :from   [:artifact]
                :where  [:and
                         [:= :group_id group-id]
                         [:= :artifact_id artifact-id]]}]
    (->> (sql/format sqlmap)
         (jdbc/execute-one! db)
         (boolean))))


(defn get-github-urls
  [db]
  (let [sqlmap {:select [:homepage]
                :from   [:artifact]
                :where  [:like :homepage "%github%"]}]
    (->> (sql/format sqlmap)
         (jdbc/execute! db)
         (mapv :artifact/homepage))))

