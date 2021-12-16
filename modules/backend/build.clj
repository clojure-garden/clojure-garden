(ns build
  (:refer-clojure :exclude [test])
  (:require
    [clojure.pprint :as pprint]
    [clojure.set :as set]
    [clojure.tools.build.util.file :as file]
    [org.corfield.build :as bb]))


(def defaults
  {:src-dirs       ["src"]
   :resource-dirs  ["resources"]
   :lib            'garden.clojure/backend
   :main           'platform.main
   :target         "target"
   :coverage-dir   "coverage"
   :uber-file      "target/backend.jar"
   :build-meta-dir "resources/platform"})



(defn pretty-print
  [x]
  (binding [pprint/*print-right-margin* 130]
    (pprint/pprint x)))



(defn with-defaults
  [opts]
  (merge defaults opts))



(defn extract-meta
  [opts]
  (-> opts
      (select-keys [:lib
                    :version
                    :build-number
                    :build-timestamp
                    :git-url
                    :git-branch
                    :git-sha])
      (set/rename-keys {:lib :module})
      (update :module str)))



(defn write-meta
  [opts]
  (let [dir (:build-meta-dir opts)]
    (file/ensure-dir dir)
    (->> opts
         (extract-meta)
         (pretty-print)
         (with-out-str)
         (spit (format "%s/build.edn" dir)))))



(defn outdated
  [opts]
  (-> opts
      (with-defaults)
      (bb/run-task [:outdated])))



(defn outdated:upgrade
  [opts]
  (-> opts
      (with-defaults)
      (bb/run-task [:outdated :outdated/upgrade])))



(defn clean
  [opts]
  (-> opts
      (with-defaults)
      (bb/clean)))



(defn repl
  [opts]
  (let [opts (with-defaults opts)]
    (write-meta opts)
    (bb/run-task opts [:test :develop])))



(defn test
  [opts]
  (let [opts (with-defaults opts)]
    (write-meta opts)
    (bb/run-task opts [:test])))


(defn test:unit
  [opts]
  (let [opts (with-defaults opts)]
    (write-meta opts)
    (bb/run-task opts [:test :test/unit])))


(defn test:integration
  [opts]
  (let [opts (with-defaults opts)]
    (write-meta opts)
    (bb/run-task opts [:test :test/b])))


(defn uber
  [opts]
  (let [opts (with-defaults opts)]
    (write-meta opts)
    (-> opts
        (assoc :scm {:url (:git-url opts)
                     :tag (:version opts)})
        (bb/uber))))
