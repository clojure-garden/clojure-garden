(ns platform.github.repository
  (:require
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
    (mapv (fn [{:as repository :keys [id]}]
            (let [languages (-> (github-sql/select-languages-by-id datasource id)
                                (transform-languages))
                  topics    (-> (github-sql/select-topics-by-id datasource id)
                                (transform-topics))]
              (merge repository {:languages languages :topics topics})))
          repositories)))


(defn count-repositories
  [datasource filters]
  (-> (github-sql/select-repositories datasource filters)
      (first)
      (:aggregate)))
