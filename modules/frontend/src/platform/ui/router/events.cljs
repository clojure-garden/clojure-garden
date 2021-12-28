(ns platform.ui.router.events
  "Router events."
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [lambdaisland.glogi :as log]
    [re-frame.core :as rf]
    [reitit.frontend.controllers :as rfc]
    [reitit.frontend.easy :as rfe]))


(rf/reg-event-db
  :navigation/set-router
  (fn-traced [db [_ router]]
    (assoc-in db [:navigation :router] router)))


(rf/reg-event-fx
  :navigation/set-route
  (fn-traced [{db :db} [_ route]]
    (let [initialized?   (get-in db [:app :initialized?])
          authenticated? (some? (:user db))
          private?       (get-in route [:data :private] true)]
      (cond
        ;; if app is not initialized
        (not initialized?)
        {:dispatch-later [{:ms 100, :dispatch [:navigation/set-route route]}]}

        ;; if route is not found
        (nil? route)
        (if-not authenticated?
          {:navigation/redirect {:route-name :page/landing}}
          {:navigation/redirect {:route-name :page/dashboard}})

        ;; if route is private and user is not authenticated
        (and private? (not authenticated?))
        {:navigation/redirect {:route-name :page/landing}
         :notification        {:level           :error
                               :i18n/translator (get-in db [:i18n :translator])
                               :i18n/key        :auth/unauthorized}}

        ;; if route is not private or user is authenticated
        (or (not private?) authenticated?)
        (let [old-route        (:router db)
              old-controllers  (:controllers old-route)
              next-controllers (rfc/apply-controllers old-controllers route)
              next-route       (assoc route :controllers next-controllers)]
          {:db (assoc-in db [:navigation :route] next-route)})

        ;; otherwise, redirect to landing page
        :else {:navigation/redirect {:route-name :page/landing}}))))


(rf/reg-fx
  :navigation/redirect
  (fn [{:keys [route-name params query]}]
    (log/trace :msg "Router redirect" :route-name route-name :params params :query query)
    (rfe/push-state route-name params query)))


(rf/reg-event-fx
  :navigation/redirect
  (fn-traced [_ [_ {:keys [route-name params query]}]]
    {:navigation/redirect {:route-name route-name, :params params, :query query}}))


(rf/reg-fx
  :navigation/replace
  (fn [{:keys [route-name params query]}]
    (log/trace :msg "Router replace" :route-name route-name :params params :query query)
    (rfe/replace-state route-name params query)))


(rf/reg-event-fx
  :navigation/replace
  (fn-traced [_ [_ {:keys [route-name params query]}]]
    {:navigation/replace {:route-name route-name, :params params, :query query}}))
