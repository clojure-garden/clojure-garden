(ns antd.icons
  (:require
    ["@ant-design/icons/es/icons/DownloadOutlined" :default antd.icons.download-outlined]
    ["@ant-design/icons/es/icons/ForkOutlined" :default antd.icons.fork-outlined]
    ["@ant-design/icons/es/icons/FormatPainterOutlined" :default antd.icons.format-painter-outlined]
    ["@ant-design/icons/es/icons/GithubOutlined" :default antd.icons.github-outlined]
    ["@ant-design/icons/es/icons/LoadingOutlined" :default antd.icons.loading-outlined]
    ["@ant-design/icons/es/icons/LogoutOutlined" :default antd.icons.logout-outlined]
    ["@ant-design/icons/es/icons/SettingOutlined" :default antd.icons.setting-outlined]
    ["@ant-design/icons/es/icons/StarOutlined" :default antd.icons.star-outlined]
    ["@ant-design/icons/es/icons/TeamOutlined" :default antd.icons.team-outlined]
    ["@ant-design/icons/es/icons/UserOutlined" :default antd.icons.user-outlined]
    [reagent.core :as r]))


(def download-outlined (r/adapt-react-class antd.icons.download-outlined))
(def format-painter-outlined (r/adapt-react-class antd.icons.format-painter-outlined))
(def github-outlined (r/adapt-react-class antd.icons.github-outlined))
(def star-outlined (r/adapt-react-class antd.icons.star-outlined))
(def team-outlined (r/adapt-react-class antd.icons.team-outlined))
(def fork-outlined (r/adapt-react-class antd.icons.fork-outlined))
(def loading-outlined (r/adapt-react-class antd.icons.loading-outlined))
(def logout-outlined (r/adapt-react-class antd.icons.logout-outlined))
(def setting-outlined (r/adapt-react-class antd.icons.setting-outlined))
(def user-outlined (r/adapt-react-class antd.icons.user-outlined))
