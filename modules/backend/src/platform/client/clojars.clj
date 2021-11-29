(ns platform.client.clojars
  (:require
    [camel-snake-kebab.core :as csk]
    [clojure.string :as s]
    [clojure.xml :as xml]
    [platform.client.common :refer [request-rest build-url safe]]))


(def ^:private clojars-repo-url "https://clojars.org/repo")


(def ^:private clojars-rest-url "https://clojars.org/api")


(def ^:private new-artifact-key-namings
  {:jar-name :artifact-id
   :group-name :group-id
   :user :owner})


(defn- find-first
  [pred coll]
  (some #(when (pred %)
           %)
        coll))


(defn- content-vec->hash-map
  [content]
  (reduce (fn [acc {k :tag [v] :content}]
            (assoc acc (csk/->kebab-case-keyword k) v))
          {} content))


(defn parse-artifact-url
  "Extract the group ID and the artifact ID from the specified url."
  [url]
  (let [re #"^http(?:s)?:\/\/(?:www\.)?clojars\.org(?:\/([\w,\-,\_,\.]+))?\/([\w,\-,\_,\.]+)\/?$"]
    (when-let [matches (re-matches re url)]
      (into [] (rest matches)))))


(defn get-package-identifier
  [url]
  "Extract the group ID and the artifact ID from the specified url.
   If the group ID is missing from the URL, it will copy it from the artifact ID."
  (let [[group-id artifact-id] (parse-artifact-url url)]
    [(or group-id artifact-id) artifact-id]))


(defn on-clojars-get-group-artifacts-handler
  [{:keys [data errors]}]
  (if-not errors
    (mapv #(clojure.set/rename-keys % new-artifact-key-namings) data)
    (throw (ex-info "Request to Clojars failed: can't fetch group artifacts"
                    {:errors errors}))))


(defn on-clojars-get-artifact-handler
  [{:keys [data errors]}]
  (if-not errors
    (clojure.set/rename-keys data new-artifact-key-namings)
    (throw (ex-info "Request to Clojars failed: can't fetch artifact"
                    {:errors errors}))))


(defn get-group-artifacts
  "Retrieve information about the specified group of artifacts."
  [group-id]
  (-> (build-url clojars-rest-url "groups" group-id)
      (request-rest on-clojars-get-group-artifacts-handler)))


(defn get-artifact
  "Retrieve information about the specified artifact."
  ([artifact-id]
   (get-artifact artifact-id artifact-id))
  ([group-id artifact-id]
   (-> (build-url clojars-rest-url "artifacts" group-id artifact-id)
       (request-rest on-clojars-get-artifact-handler))))


(defn get-pom-objects
  "Retrieve a list of pom objects from the xml sequence."
  [pom key-tag]
  (->> (:content pom)
       (find-first #(= (:tag %) key-tag))
       :content
       (mapv #(content-vec->hash-map (:content %)))))


(defn get-artifact-pom
  "Retrieve the pom file of the artifact and return it as a xml sequence."
  ([artifact-id version]
   (get-artifact-pom artifact-id artifact-id version))
  ([group-id artifact-id version]
   (let [group-id-path (s/replace group-id #"\." "/")]
     (-> (build-url clojars-repo-url group-id-path artifact-id version (str artifact-id "-" version ".pom"))
         xml/parse
         xml-seq
         first))))


(defn get-pom-extracts
  "Retrieve dependencies and licenses from the specified pom file."
  [group-id artifact-id version]
  (if-not (s/includes? version "SNAPSHOT")
    (let [pom (get-artifact-pom group-id artifact-id version)]
      (hash-map :licenses (get-pom-objects pom :licenses)
                :dependencies (mapv #(dissoc % :exclusions) (get-pom-objects pom :dependencies))))
    (hash-map :licenses []
              :dependencies [])))


(defn- update-with-pom-extracts
  [group-id artifact-id versions]
  (mapv #(merge % (get-pom-extracts group-id artifact-id (:version %))) versions))


(defn get-artifact-info
  "Retrieve information (owner, description, homepage, the latest release, list of recent versions
   and dependencies, license info) about the artifact by the Clojars URL."
  [url]
  (let [[group-id artifact-id] (get-package-identifier url)
        base-info (get-artifact group-id artifact-id)]
    (update base-info :recent-versions (partial update-with-pom-extracts group-id artifact-id))))
