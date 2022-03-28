(ns platform.clojars.clojars-sql
  (:require
    [honey.sql.helpers :as helpers]
    [platform.storage.jdbc-wrappers :as jw])
  (:import
    (com.zaxxer.hikari
      HikariDataSource)
    (java.util
      UUID)))


(defn- insert-artifact!
  [^HikariDataSource db {:as _artifact :keys [artifact-id group-id homepage description owner
                                              latest-version latest-release downloads from-clojars]}]
  (jw/execute! db (-> (helpers/insert-into :artifact)
                      (helpers/values [{:id             (UUID/randomUUID)
                                        :artifact-id    artifact-id
                                        :group-id       group-id
                                        :homepage       homepage
                                        :description    description
                                        :owner          owner
                                        :latest-version latest-version
                                        :latest-release latest-release
                                        :downloads      downloads
                                        :from-clojars   from-clojars}])
                      (helpers/returning :id)
                      jw/sql-format)))


(defn- insert-license!
  [^HikariDataSource db {:as _license :keys [name url]}]
  (jw/execute! db (-> (helpers/insert-into :license)
                      (helpers/values [{:id   (UUID/randomUUID)
                                        :name name
                                        :url  url}])
                      (helpers/on-conflict :name)
                      (helpers/do-update-set :name)
                      (helpers/returning :id)
                      jw/sql-format)))


(defn- insert-version-license!
  [^HikariDataSource db version-id license-id]
  (jw/execute! db (-> (helpers/insert-into :version-license)
                      (helpers/values [{:version-id version-id
                                        :license-id license-id}])
                      jw/sql-format)))


(defn- insert-licenses!
  [^HikariDataSource db version-id licenses]
  (when-not (empty? licenses)
    (doall
      (map (fn [license]
             (let [[{license-id :id}] (insert-license! db license)]
               (insert-version-license! db version-id license-id)))
           licenses))))


(defn- insert-version!
  [^HikariDataSource db artifact-id {:as _artifact-version :keys [version downloads]}]
  (jw/execute! db (-> (helpers/insert-into :version)
                      (helpers/values [{:id          (UUID/randomUUID)
                                        :name        version
                                        :downloads   downloads
                                        :artifact-id artifact-id}])
                      (helpers/returning :id)
                      jw/sql-format)))


(defn- insert-version-info!
  [^HikariDataSource db artifact-id {:as version-info :keys [licenses]}]
  (let [[{version-id :id}] (insert-version! db artifact-id version-info)]
    (insert-licenses! db version-id licenses)))


(defn- insert-versions!
  [^HikariDataSource db artifact-id versions]
  (when-not (empty? versions)
    (doall
      (map #(insert-version-info! db artifact-id %) versions))))


(defn insert-artifact-info!
  [^HikariDataSource db artifact-info from-clojars]
  (jw/with-transaction [tx db]
    (let [artifact-info (conj artifact-info [:from-clojars from-clojars])
          [{artifact-id :id}] (insert-artifact! tx artifact-info)]
      (insert-versions! tx artifact-id (:recent-versions artifact-info)))))


(defn artifact-exists?
  [^HikariDataSource db group-id artifact-id]
  (let [sqlmap {:select [1]
                :from   [:artifact]
                :where  [:and
                         [:= :group-id group-id]
                         [:= :artifact-id artifact-id]]}]
    (->> (jw/sql-format sqlmap)
         (jw/execute-one! db)
         (boolean))))


(defn get-github-urls
  [^HikariDataSource db]
  (let [sqlmap {:select [:homepage]
                :from   [:artifact]
                :where  [:like :homepage "%github%"]}]
    (->> (jw/sql-format sqlmap)
         (jw/execute! db)
         (mapv :homepage))))


(defn select-downloads-by-homepage
  [^HikariDataSource db {:keys [owner name]}]
  (let [homepage (format "%s/%s" owner name)
        sqlmap {:select [:downloads]
                :from   [:artifact]
                :where  [:or
                         [:like :artifact/homepage (str "%" homepage "%")]
                         [:and
                          [:like :artifact/artifact-id name]
                          [:like :artifact/group-id owner]]]}]
    (->> (jw/sql-format sqlmap)
         (jw/execute-one! db)
         (:downloads))))


(defn select-libraries
  [^HikariDataSource db]
  (let [sqlmap {:select [:id :group-id :artifact-id]
                :from   [:library]}]
    (->> (jw/sql-format sqlmap)
         (jw/execute! db))))
