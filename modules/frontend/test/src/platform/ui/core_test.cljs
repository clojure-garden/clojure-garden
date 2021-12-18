(ns platform.ui.core-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [platform.ui.core :as sut]))


(deftest square-test
  (testing "dummy test"
    (is (= 4 (sut/square 2)))))
