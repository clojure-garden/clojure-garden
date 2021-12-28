(ns platform.ui.router.subs
  (:require
    [re-frame.core :as rf]
    [reitit.frontend.easy :as rfe]))


(rf/reg-sub
  :navigation/router
  (fn [db _]
    (:router db)))


(rf/reg-sub
  :navigation/route
  (fn [db _]
    (get-in db [:navigation :route])))


(rf/reg-sub
  :navigation/route-name
  :<- [:navigation/route]
  (fn [route]
    (get-in route [:data :name])))


(rf/reg-sub
  :navigation/route-params
  :<- [:navigation/route]
  (fn [route]
    (:parameters route)))


(rf/reg-sub
  :href
  (fn [_ [_ & args]]
    (apply rfe/href args)))
