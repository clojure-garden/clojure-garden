(ns platform.client.clojars-test
  (:require
    [clojure.test :refer :all]
    [platform.client.clojars :refer [parse-artifact-url]]))


(deftest test-parse-repository-url
  (testing "an empty string"
    (is (= nil
           (parse-artifact-url ""))))
  (testing "a random string"
    (is (= nil
           (parse-artifact-url "clojars"))))
  (testing "a repository url containing a domain name other than clojars.org"
    (is (= nil
           (parse-artifact-url "https://github.com/alexgherega/suitable"))))
  (testing "a repository url containing a protocol separator other than ://"
    (is (= nil
           (parse-artifact-url "https@clojars.org/alexgherega/suitable"))))
  (testing "a repository url containing a protocol name other than http(s)"
    (is (= nil
           (parse-artifact-url "ftp://clojars.org/alexgherega/suitable"))))
  (testing "a repository url that does not contain the protocol name"
    (is (= nil
           (parse-artifact-url "clojars.org/alexgherega/suitable"))))
  (testing "a repository url that does not contain the group/artifact name"
    (is (= nil
           (parse-artifact-url "https://clojars.org"))))
  (testing "a repository url containing an empty artifact name"
    (is (= nil
           (parse-artifact-url "https://clojars.org/"))))
  (testing "a repository url containing more than two subdirectories"
    (is (= nil
           (parse-artifact-url "https://clojars.org/alexgherega/suitable/some"))))
  (testing "a repository url containing only the artifact name"
    (is (= [nil "figwheel"]
           (parse-artifact-url "https://clojars.org/figwheel"))))
  (testing "a repository url containing the artifact group & the artifact name"
    (is (= ["alexgherega" "suitable"]
           (parse-artifact-url "https://clojars.org/alexgherega/suitable"))))
  (testing "a repository url containing a trailing slash"
    (is (= ["alexgherega" "suitable"]
           (parse-artifact-url "https://clojars.org/alexgherega/suitable/"))))
  (testing "a repository url containing the WWW subdomain"
    (is (= ["alexgherega" "suitable"]
           (parse-artifact-url "https://www.clojars.org/alexgherega/suitable"))))
  (testing "a repository url containing the HTTP protocol"
    (is (= ["alexgherega" "suitable"]
           (parse-artifact-url "http://clojars.org/alexgherega/suitable"))))
  (testing "a repository url containing the HTTPs protocol"
    (is (= ["alexgherega" "suitable"]
           (parse-artifact-url "https://clojars.org/alexgherega/suitable"))))
  (testing "a group name containing dashes"
    (is (= ["oalex-gherega" "suitable"]
           (parse-artifact-url "https://clojars.org/oalex-gherega/suitable"))))
  (testing "a group name containing underscores"
    (is (= ["oalex_gherega" "suitable"]
           (parse-artifact-url "https://clojars.org/oalex_gherega/suitable"))))
  (testing "a group name containing dots"
    (is (= ["org.rksm" "suitable"]
           (parse-artifact-url "https://clojars.org/org.rksm/suitable"))))
  (testing "a group name containing numbers"
    (is (= ["rksm8" "suitable"]
           (parse-artifact-url "https://clojars.org/rksm8/suitable"))))
  (testing "an artifact name containing dashes"
    (is (= ["alexeypopov" "clj-nats-async"]
           (parse-artifact-url "https://clojars.org/alexeypopov/clj-nats-async"))))
  (testing "an artifact name containing underscores"
    (is (= ["alexeypopov" "clj_nats_async"]
           (parse-artifact-url "https://clojars.org/alexeypopov/clj_nats_async"))))
  (testing "an artifact name containing numbers"
    (is (= ["cljsjs" "d3"]
           (parse-artifact-url "https://clojars.org/cljsjs/d3"))))
  (testing "case sensitivity"
    (is (= ["Leiningen" "Leiningen"]
           (parse-artifact-url "https://clojars.org/Leiningen/Leiningen")))))
