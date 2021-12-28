(ns antd.core
  (:require
    ["antd/es/avatar" :default antd.avatar]
    ["antd/es/badge" :default antd.badge]
    ["antd/es/config-provider" :default antd.config-provider]
    ["antd/es/divider" :default antd.divider]
    ["antd/es/dropdown" :default antd.dropdown]
    ["antd/es/image" :default antd.image]
    ["antd/es/input" :default antd.input]
    ["antd/es/layout" :default antd.layout]
    ["antd/es/menu" :default antd.menu]
    ["antd/es/notification" :default antd.notification]
    ["antd/es/page-header" :default antd.page-header]
    ["antd/es/space" :default antd.space]
    ["antd/es/spin" :default antd.spin]
    ["antd/es/typography" :default antd.typography]
    [reagent.core :as r]))


;;
;; Locale
;;

(def config-provider (r/adapt-react-class antd.config-provider))



;;
;; Avatar
;;

(def avatar (r/adapt-react-class antd.avatar))



;;
;; Badge
;;

(def badge (r/adapt-react-class antd.badge))



;;
;; Divider
;;

(def divider (r/adapt-react-class antd.divider))



;;
;; Dropdown
;;

(def dropdown (r/adapt-react-class antd.dropdown))
(def dropdown-button (r/adapt-react-class (.-Button antd.dropdown)))



;;
;; Input
;;

(def input (r/adapt-react-class (.-TextArea antd.input)))
(def input-raw (r/adapt-react-class antd.input))
(def input-group (r/adapt-react-class (.-TextArea antd.input)))
(def input-group-raw (r/adapt-react-class (.-Group antd.input)))
(def input-password (r/adapt-react-class (.-TextArea antd.input)))
(def input-password-raw (r/adapt-react-class (.-Password antd.input)))
(def input-search (r/adapt-react-class (.-TextArea antd.input)))
(def input-search-raw (r/adapt-react-class (.-Search antd.input)))
(def input-text-area (r/adapt-react-class (.-TextArea antd.input)))
(def input-text-area-raw (r/adapt-react-class (.-TextArea antd.input)))



;;
;; Image
;;

(def image (r/adapt-react-class antd.image))
(def image-preview-group (r/adapt-react-class (.-PreviewGroup antd.image)))



;;
;; Layout
;;

(def layout (r/adapt-react-class antd.layout))
(def layout-content (r/adapt-react-class (.-Content antd.layout)))
(def layout-footer (r/adapt-react-class (.-Footer antd.layout)))
(def layout-header (r/adapt-react-class (.-Header antd.layout)))
(def layout-sider (r/adapt-react-class (.-Sider antd.layout)))



;;
;; Notification
;;

(def close-notification (comp (.-close antd.notification) clj->js))
(def config-notification (comp (.-config antd.notification) clj->js))
(def destroy-notification (comp (.-destroy antd.notification) clj->js))
(def error-notification (comp (.-error antd.notification) clj->js))
(def info-notification (comp (.-info antd.notification) clj->js))
(def open-notification (comp (.-open antd.notification) clj->js))
(def success-notification (comp (.-success antd.notification) clj->js))
(def warning-notification (comp (.-warning antd.notification) clj->js))



;;
;; Page header
;;

(def page-header (r/adapt-react-class antd.page-header))



;;
;; Menu
;;

(def menu (r/adapt-react-class antd.menu))
(def menu-divider (r/adapt-react-class (.-Divider antd.menu)))
(def menu-item (r/adapt-react-class (.-Item antd.menu)))
(def menu-item-group (r/adapt-react-class (.-ItemGroup antd.menu)))
(def menu-sub-menu (r/adapt-react-class (.-SubMenu antd.menu)))



;;
;; Space
;;

(def space (r/adapt-react-class antd.space))



;;
;; Spinner
;;

(def spin (r/adapt-react-class antd.spin))



;;
;; Typography
;;

(def typography (r/adapt-react-class antd.typography))
(def typography-text (r/adapt-react-class (.-Text antd.typography)))
(def typography-title (r/adapt-react-class (.-Title antd.typography)))
(def typography-paragraph (r/adapt-react-class (.-Paragraph antd.typography)))
(def typography-link (r/adapt-react-class (.-Link antd.typography)))
