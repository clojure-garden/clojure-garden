(ns platform.utils.tagger
  (:require
    [camel-snake-kebab.core :as csk]
    [clj-yaml.core :as yaml]
    [clojure.string :as str]))


(defn read-clojure-toolbox-data
  []
  (try
    (->> "https://raw.githubusercontent.com/weavejester/clojure-toolbox.com/master/projects.yml"
         (slurp)
         (yaml/parse-string)
         (reduce
           (fn [acc [project opts]]
             (as-> (into {} opts) m
                   (update m :categories (fn [coll] (map str/lower-case coll)))
                   (assoc! acc (csk/->kebab-case-keyword project) m)))
           (transient {}))
         (persistent!))
    (catch Exception _
      [])))
