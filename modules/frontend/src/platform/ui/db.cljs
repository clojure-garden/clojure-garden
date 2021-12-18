(ns platform.ui.db
  "UI state."
  (:require
    [re-frame.core :as rf]))


(rf/reg-event-fx
  ::init
  (fn [_ _]
    {}))
