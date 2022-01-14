(ns platform.api.v1.pagination
  (:require
    [clojure.string :as str]))


(defn build-pagination-meta
  [host urn query-string current-page per-page total]
  (let [uri        (str "https://" host urn "?" (when query-string (str/replace query-string #"&page=.*" "")) "&page=")
        page-count (-> (/ total per-page)
                       (Math/ceil)
                       (long))]
    {:page current-page
     :per-page per-page
     :page-count page-count
     :total-count total
     :links (cond-> []
              (> current-page 1)          (conj {:first    (str uri 1)}
                                                {:previous (str uri (dec current-page))})
              (< current-page page-count) (conj {:next     (str uri (inc current-page))}
                                                {:last     (str uri page-count)}))}))


(defn build-uri-references
  [links]
  (mapv (fn [link]
          (let [[label uri] (first link)]
            (str "<" uri ">" ";" " rel=\"" (name label) "\"")))
        links))


(defn build-link-header
  [links]
  (->> links
       (build-uri-references)
       (str/join ", ")))
