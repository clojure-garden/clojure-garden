(ns platform.github.api-client-test
  (:require
    [clojure.test :refer [deftest testing is]]
    [platform.github.api-client :refer [parse-repository-url]]))


(deftest ^:integration dummy-test
  (is (= 2 2)))


(deftest ^:unit test-parse-repository-url
  (testing "an empty string"
    (is (= nil
           (parse-repository-url ""))))
  (testing "a random string"
    (is (= nil
           (parse-repository-url "github"))))
  (testing "a repository url containing a domain name other than github.com"
    (is (= nil
           (parse-repository-url "https://clojars.org/trinitycore/trinitycore"))))
  (testing "a repository url containing a protocol separator other than :// or @"
    (is (= nil
           (parse-repository-url "https/github.com/trinitycore/trinitycore"))))
  (testing "a repository url containing protocol other than http(s) or ssh"
    (is (= nil
           (parse-repository-url "ftp://github.com/trinitycore/trinitycore"))))
  (testing "a repository url that does not contain the protocol name"
    (is (= nil
           (parse-repository-url "github.com/trinitycore/trinitycore"))))
  (testing "a repository url that does not contain the repository owner/name"
    (is (= nil
           (parse-repository-url "https://github.com"))))
  (testing "a repository url containing an empty owner/name"
    (is (= nil
           (parse-repository-url "https://github.com/"))))
  (testing "a repository url containing more than two subdirectories"
    (is (= nil
           (parse-repository-url "https://github.com/trinitycore/trinitycore/src"))))
  (testing "a repository url containing the repository owner & the repository name"
    (is (= ["trinitycore" "trinitycore"]
           (parse-repository-url "https://github.com/trinitycore/trinitycore"))))
  (testing "a repository url containing a trailing slash"
    (is (= ["trinitycore" "trinitycore"]
           (parse-repository-url "https://github.com/trinitycore/trinitycore/"))))
  (testing "a repository url containing the WWW subdomain"
    (is (= ["trinitycore" "trinitycore"]
           (parse-repository-url "https://www.github.com/trinitycore/trinitycore"))))
  (testing "a repository url containing the SSH protocol"
    (is (= ["trinitycore" "trinitycore"]
           (parse-repository-url "git@github.com:trinitycore/trinitycore.git"))))
  (testing "a repository url containing the HTTP protocol"
    (is (= ["trinitycore" "trinitycore"]
           (parse-repository-url "http://github.com/trinitycore/trinitycore"))))
  (testing "a repository url containing the HTTPs protocol"
    (is (= ["trinitycore" "trinitycore"]
           (parse-repository-url "https://github.com/trinitycore/trinitycore"))))
  (testing "an owner name containing dashes"
    (is (= ["trinity-core" "trinitycore"]
           (parse-repository-url "https://github.com/trinity-core/trinitycore"))))
  (testing "an owner name containing underscores"
    (is (= ["trinity_core" "trinitycore"]
           (parse-repository-url "https://github.com/trinity_core/trinitycore"))))
  (testing "an owner name containing numbers"
    (is (= ["trinitycore8" "trinitycore"]
           (parse-repository-url "https://github.com/trinitycore8/trinitycore"))))
  (testing "an owner name containing dots"
    (is (= ["trinity.core" "trinitycore"]
           (parse-repository-url "https://github.com/trinity.core/trinitycore"))))
  (testing "an repository name containing dashes"
    (is (= ["trinitycore" "trinity-core"]
           (parse-repository-url "https://github.com/trinitycore/trinity-core"))))
  (testing "an repository name containing underscores"
    (is (= ["trinitycore" "trinity_core"]
           (parse-repository-url "https://github.com/trinitycore/trinity_core"))))
  (testing "an repository name containing numbers"
    (is (= ["trinitycore" "trinitycore8"]
           (parse-repository-url "https://github.com/trinitycore/trinitycore8"))))
  (testing "an repository name containing dots"
    (is (= ["trinitycore" "trinity.core"]
           (parse-repository-url "https://github.com/trinitycore/trinity.core"))))
  (testing "case sensitivity"
    (is (= ["TrinityCore" "Parser"]
           (parse-repository-url "https://github.com/TrinityCore/Parser")))))
