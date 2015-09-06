(ns clevolution.app.currentimagetab
  (:use [mikera.cljutils.error])
  (:require
    [seesaw.core :as seesaw]
    [seesaw.widget-options :refer [widget-option-provider]]
    [seesaw.border :refer [line-border]]
    [clevolution.file-input :refer [read-image-from-file]]
    [clevolution.file-output :refer :all]
    [clevolution.app.imagefunctions :refer [to-display-size make-image-icon]]
    [clevolution.app.appstate :as appstate :refer [app-state]])
  (:import
    [java.awt Color]))


(defn make-current-image-component
  [image]
  (let [icon (make-image-icon image
                              (:image-display-size @appstate/app-state))]
    (seesaw/border-panel :id :current-image-component
                         :background Color/LIGHT_GRAY
                         :center icon)))

(defn replace-image
  [current-image-component image]
  (let [icon (make-image-icon image
                              (:image-display-size @appstate/app-state))]
    (.removeAll current-image-component)
    (.add current-image-component icon)))

