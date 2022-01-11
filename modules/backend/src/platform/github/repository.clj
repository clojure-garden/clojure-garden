(ns platform.github.repository
  (:require
    [platform.common :as common]))


(defn get-code-base-size
  [languages]
  (reduce (fn [acc {:keys [size]}]
            (+' acc size)) 0 languages))


(defn update-languages
  [languages]
  (let [code-base-size (get-code-base-size languages)]
    (mapv (fn [{:as language :keys [size]}]
            (-> language
                (assoc :size-percentage (common/get-percentage size code-base-size))
                (dissoc :size)))
          languages)))


(defn preprocess-repository
  [repository]
  (update repository :languages update-languages))


(defn preprocess-repositories
  [repositories]
  (mapv preprocess-repository repositories))
