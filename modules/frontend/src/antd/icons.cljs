(ns antd.icons
  (:require
    ["@ant-design/icons/es/icons/DownOutlined" :default antd.icons.down-outlined]
    ["@ant-design/icons/es/icons/GithubOutlined" :default antd.icons.github-outlined]
    ["@ant-design/icons/es/icons/LoadingOutlined" :default antd.icons.loading-outlined]
    ["@ant-design/icons/es/icons/LogoutOutlined" :default antd.icons.logout-outlined]
    ["@ant-design/icons/es/icons/SettingOutlined" :default antd.icons.setting-outlined]
    ["@ant-design/icons/es/icons/UserOutlined" :default antd.icons.user-outlined]
    [reagent.core :as r]))


(def down-outlined (r/adapt-react-class antd.icons.down-outlined))
(def github-outlined (r/adapt-react-class antd.icons.github-outlined))
(def loading-outlined (r/adapt-react-class antd.icons.loading-outlined))
(def logout-outlined (r/adapt-react-class antd.icons.logout-outlined))
(def setting-outlined (r/adapt-react-class antd.icons.setting-outlined))
(def user-outlined (r/adapt-react-class antd.icons.user-outlined))
