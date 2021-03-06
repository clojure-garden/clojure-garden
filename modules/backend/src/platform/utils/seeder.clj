(ns platform.utils.seeder
  (:require
    [clj-yaml.core :as yaml]
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [clojure.walk :as clj-walk]
    [java-time :as jt]
    [platform.clojars.clojars-sql :as clj-sql]
    [platform.common :as common]
    [platform.github.api-client :as github-client]))


(def ^:private clojars-url "https://clojars.org")


(defn- disorder
  [ordering-map map-fn]
  (clj-walk/postwalk #(if (map? %) (into map-fn %) %) ordering-map))


(defn- extract-github-project-urls
  [repositories]
  (reduce-kv (fn [res _ {:keys [url]}]
               (conj res url))
             [] repositories))


(defn- extract-artifact-urls
  [artifacts]
  (reduce (fn [acc {:keys [group-id artifact-id]}]
            (conj acc (common/build-url clojars-url group-id artifact-id)))
          [] artifacts))


(defn- get-monthly-time-periods
  [start-date end-date]
  (->> (jt/iterate jt/plus start-date (jt/months 1))
       (take-while #(jt/before? % end-date))
       (partition 2 1)
       (map vec)))


(defn- get-github-monthly-search-queries
  [base-query partition-param start-date end-date]
  (->> (get-monthly-time-periods start-date end-date)
       (map #(str base-query " " partition-param ":" (first %) ".." (jt/minus (last %) (jt/days 1))))))


(defn- get-github-project-urls-from-search*
  [search-query]
  (->> search-query
       github-client/search-repositories
       (reduce (fn [acc {:keys [url]}]
                 (conj acc url))
               [])))


(defn get-github-project-urls-from-edn
  [url]
  (-> url
      slurp
      edn/read-string))


(defn get-github-project-urls-from-yml
  [url]
  (-> url
      slurp
      yaml/parse-string
      (disorder {})
      extract-github-project-urls))


(defn get-github-project-urls-from-search
  [search-query]
  (let [start-date (jt/local-date 2008 1 1)
        end-date (jt/plus (jt/local-date) (jt/months 1))]
    (->> (get-github-monthly-search-queries search-query "created" start-date end-date)
         (map get-github-project-urls-from-search*)
         flatten
         vec)))


(defn get-artifact-urls
  []
  (-> (io/resource "database/seeding/clojars.edn")
      slurp
      edn/read-string
      extract-artifact-urls))


(defn get-repository-urls
  [db]
  (let [tool-box-urls (get-github-project-urls-from-yml (io/resource "database/seeding/tool-box-urls.yml"))
        clojure-doc-urls (get-github-project-urls-from-edn (io/resource "database/seeding/custom-urls.edn"))
        clojars-urls (clj-sql/get-github-urls db)]
    (into [] (distinct (concat tool-box-urls
                               clojure-doc-urls
                               clojars-urls)))))
