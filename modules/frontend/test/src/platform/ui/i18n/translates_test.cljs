(ns platform.ui.i18n.translates-test
  (:require
    [cljs.test :refer [deftest testing is]]
    [clojure.set :as set]
    [platform.ui.i18n.translates :as sut]
    [platform.ui.utils.string :refer [format]]))


(deftest dictionaries-test
  (testing "all keys should be present in all dictionaries"
    (let [dictionaries (dissoc sut/dictionaries :tongue/fallback)
          all-keys     (->> dictionaries
                            (vals)
                            (mapcat keys)
                            (set))]
      (doseq [[language dictionary] dictionaries]
        (let [dictionary-keys (set (keys dictionary))
              problems        (set/difference all-keys dictionary-keys)]
          (is (empty? problems) (format "There are some keys missing in the `%s` dictionary" language)))))))
