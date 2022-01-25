(ns platform.github.repository
  (:require
    [platform.clojars.clojars-sql :as clojars-sql]
    [platform.common :as common]
    [platform.github.github-sql :as github-sql]))


(defn get-code-base-size
  [languages]
  (reduce (fn [acc {:keys [size]}]
            (+' acc size))
          0
          languages))


(defn transform-languages
  [languages]
  (let [code-base-size (get-code-base-size languages)]
    (mapv (fn [{:as language :keys [size]}]
            (-> language
                (assoc :size-percentage (common/get-percentage size code-base-size))
                (dissoc :size)))
          languages)))


(defn transform-topics
  [topics]
  (reduce (fn [acc {:keys [name]}]
            (conj acc name))
          []
          topics))


(defn get-repositories
  [datasource filters limit offset]
  (let [repositories (github-sql/select-repositories datasource filters limit offset)]
    (mapv (fn [{:as repository :keys [id owner name]}]
            (let [languages (-> (github-sql/select-languages-by-id datasource id)
                                (transform-languages))
                  topics    (-> (github-sql/select-topics-by-id datasource id)
                                (transform-topics))
                  clojars-downloads (->> {:owner owner :name name}
                                         (clojars-sql/select-downloads-by-homepage datasource))]
              (cond-> repository
                clojars-downloads (update :total-downloads +' clojars-downloads)
                :always           (merge {:languages languages :topics topics}))))
          repositories)))


(defn count-repositories
  [datasource filters]
  (-> (github-sql/select-repositories datasource filters)
      (first)
      (:aggregate)))
