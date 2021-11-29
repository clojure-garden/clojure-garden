(defproject clojure-garden/backend "0.1.0-SNAPSHOT"
  :description "A tooling platform for Clojure libraries."
  :url "https://github.com/clojure-garden/clojure-garden"
  :license {}

  :min-lein-version "2.9.5"

  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/core.async "1.3.618"]
                 [org.slf4j/slf4j-log4j12 "1.7.32"]
                 [org.slf4j/slf4j-api "1.7.32"]
                 [org.clojure/tools.logging "1.1.0"]
                 [ring/ring-core "1.9.4"]
                 [ring/ring-jetty-adapter "1.9.4"]
                 [floatingpointio/graphql-builder "0.1.14"]
                 [re-graph "0.1.15"]
                 [cheshire "5.10.0"]
                 [hato "0.8.2"]
                 [camel-snake-kebab "0.4.2"]
                 [integrant "0.8.0"]
                 [metosin/reitit "0.5.15"]
                 [org.postgresql/postgresql "42.3.1"]
                 [hikari-cp "2.13.0"]
                 [migratus "1.3.5"]
                 [com.github.seancorfield/next.jdbc "1.2.737"]
                 [com.github.seancorfield/honeysql "2.1.818"]
                 [clj-commons/clj-yaml "0.7.0"]
                 [aero "1.1.6"]
                 [clojure.java-time "0.3.3"]]

  :plugins [[lein-license "1.0.0"]]

  :source-paths ["src"]

  :test-paths ["test"]

  :resource-paths ["resources"]

  :main ^:skip-aot platform.main

  :profiles {:uberjar {:aot      :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :repl    {:repl-options {:init-ns user}}
             :dev     {:dependencies  [[ring/ring-devel "1.9.4"]
                                       [integrant/repl "0.3.2"]]
                       :source-paths  ["dev/src"]
                       ;; need to add the compiled assets to the :clean-targets
                       :clean-targets ^{:protect false} ["target"]}})
