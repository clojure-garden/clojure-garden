(ns platform.ui.notification.core
  (:require
    [antd.core :as antd]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [platform.ui.utils.string :refer [format]]
    [re-frame.core :as rf]))


(rf/reg-fx
  :notification
  (fn [{:i18n/keys [translator key params]
        :keys      [level]}]
    (let [translation (translator key params)
          config      {:message translation}]
      (case level
        :info (antd/info-notification config)
        :success (antd/success-notification config)
        :warning (antd/warning-notification config)
        :error (antd/error-notification config)
        (antd/open-notification config)))))


(rf/reg-event-fx
  :notification
  (fn-traced [_ [_ opts]]
    {:notification opts}))
