(ns platform.ui.utils.string
  (:require
    [goog.string :as gstr]
    [goog.string.format]))


(defn format
  [s & args]
  (apply gstr/format s args))


(defn date-to-string
  [date]
  (subs (.toISOString date) 0 10))
