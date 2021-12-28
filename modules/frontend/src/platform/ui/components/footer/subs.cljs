(ns platform.ui.components.footer.subs
  (:require
    [platform.ui.utils.string :refer [format]]
    [re-frame.core :as rf]
    [clojure.string :as str]))


(rf/reg-sub
  ::copyright
  (fn [_ _]
    "© 2021 Freshcode LTD. All rights reserved."))


(rf/reg-sub
  ::version
  :<- [:app/build-meta]
  (fn [{:keys [version git-sha git-url]}]
    {:link  (-> git-url
                (str/replace ".git" "/releases/tag/%s")
                (format version))
     :label (format "v%s-%s" version git-sha)}))
